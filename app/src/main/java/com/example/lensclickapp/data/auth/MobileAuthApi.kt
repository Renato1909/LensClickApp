package com.example.lensclickapp.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface MobileAuthApi {
    suspend fun login(request: MobileAuthRequest): MobileSession

    suspend fun currentUser(accessToken: String): MobileUser

    suspend fun logout(accessToken: String)
}

interface HttpTransport {
    suspend fun execute(request: HttpRequest): HttpResponse
}

class UrlConnectionTransport(
    private val baseUrl: String
) : HttpTransport {
    override suspend fun execute(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        val connection = URL(baseUrl.trimEnd('/') + request.path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = request.method
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            request.headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
            if (request.body != null) {
                connection.doOutput = true
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(request.body) }
            }
            val statusCode = connection.responseCode
            val stream = if (statusCode >= 400) connection.errorStream else connection.inputStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            HttpResponse(statusCode, connection.headerFields
                .filterKeys { it != null }
                .mapKeys { it.key!! }
                .mapValues { it.value.joinToString(",") }, body)
        } catch (error: IOException) {
            throw MobileAuthException(MobileAuthErrorCode.NETWORK, cause = error)
        } finally {
            connection.disconnect()
        }
    }
}

class HttpMobileAuthApi(
    private val transport: HttpTransport
) : MobileAuthApi {
    override suspend fun login(request: MobileAuthRequest): MobileSession {
        val response = transport.execute(
            HttpRequest(
                method = "POST",
                path = "/api/mobile/auth/login",
                headers = mapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
                ),
                body = JSONObject()
                    .put("email", request.email)
                    .put("password", request.password)
                    .put("deviceName", request.deviceName)
                    .put("appVersion", request.appVersion)
                    .toString()
            )
        )
        ensureSuccess(response, 200)
        return try {
            val root = JSONObject(response.body)
            val session = root.getJSONObject("session")
            MobileSession(
                user = parseUser(root.getJSONObject("user")),
                accessToken = session.getString("accessToken"),
                expiresAt = session.getString("expiresAt")
            )
        } catch (error: Exception) {
            throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE, cause = error)
        }
    }

    override suspend fun currentUser(accessToken: String): MobileUser {
        val response = transport.execute(authenticatedRequest("GET", "/api/mobile/auth/me", accessToken))
        ensureSuccess(response, 200)
        return try {
            parseUser(JSONObject(response.body).getJSONObject("user"))
        } catch (error: Exception) {
            throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE, cause = error)
        }
    }

    override suspend fun logout(accessToken: String) {
        val response = transport.execute(authenticatedRequest("POST", "/api/mobile/auth/logout", accessToken))
        ensureSuccess(response, 200)
    }

    private fun authenticatedRequest(method: String, path: String, accessToken: String): HttpRequest =
        HttpRequest(
            method = method,
            path = path,
            headers = mapOf(
                "Accept" to "application/json",
                "Authorization" to "Bearer $accessToken"
            )
        )

    private fun ensureSuccess(response: HttpResponse, expectedStatus: Int) {
        if (response.statusCode == expectedStatus) return
        throw MobileAuthException(
            code = errorCodeFor(response.statusCode),
            statusCode = response.statusCode
        )
    }

    private fun parseUser(json: JSONObject): MobileUser = MobileUser(
        id = json.getLong("id"),
        name = json.getString("name"),
        email = json.getString("email"),
        phone = json.optString("phone").takeIf { it.isNotBlank() },
        role = json.optString("role", "client"),
        emailVerified = json.optBoolean("emailVerified", false),
        isAdmin = json.optBoolean("isAdmin", false)
    )

    private fun errorCodeFor(statusCode: Int): MobileAuthErrorCode = when (statusCode) {
        400 -> MobileAuthErrorCode.INVALID_INPUT
        401 -> MobileAuthErrorCode.UNAUTHENTICATED
        403 -> MobileAuthErrorCode.FORBIDDEN
        404 -> MobileAuthErrorCode.NOT_FOUND
        409 -> MobileAuthErrorCode.CONFLICT
        429 -> MobileAuthErrorCode.RATE_LIMITED
        in 500..599 -> MobileAuthErrorCode.SERVER
        else -> MobileAuthErrorCode.SERVER
    }
}
