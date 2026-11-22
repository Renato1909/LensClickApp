package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.MobileSession
import com.lensclick.app.domain.MobileUser
import com.lensclick.app.domain.TwoFactorGateway
import com.lensclick.app.domain.TwoFactorSetup
import com.lensclick.app.domain.TwoFactorStatus
import org.json.JSONArray
import org.json.JSONObject

class HttpTwoFactorGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : TwoFactorGateway {

    override suspend fun getStatus(): TwoFactorStatus {
        val response = transport.execute(sessionStore.bearerRequest("GET", "/api/account/2fa/status"))
        ensureHttpSuccess(response, 200)
        val json = JSONObject(response.body)
        return TwoFactorStatus(
            enabled = json.optBoolean("enabled", false),
            method = json.optString("method", "totp")
        )
    }

    override suspend fun setup(): TwoFactorSetup {
        val response = transport.execute(sessionStore.bearerRequest("POST", "/api/account/2fa/setup"))
        ensureHttpSuccess(response, 200, 201)
        val json = JSONObject(response.body)
        val codesJson = json.optJSONArray("recoveryCodes") ?: JSONArray()
        val codesList = (0 until codesJson.length()).map { codesJson.getString(it) }
        return TwoFactorSetup(
            secret = json.optString("secret"),
            otpauthUrl = json.optString("otpauthUrl"),
            recoveryCodes = codesList
        )
    }

    override suspend fun enable(code: String): Boolean {
        val body = JSONObject().put("code", code).toString()
        val response = transport.execute(sessionStore.bearerRequest("POST", "/api/account/2fa/enable", body))
        ensureHttpSuccess(response, 200)
        return JSONObject(response.body).optBoolean("success", true)
    }

    override suspend fun disable(password: String, code: String?): Boolean {
        val body = JSONObject()
            .put("password", password)
            .apply { if (!code.isNullOrBlank()) put("code", code) }
            .toString()
        val response = transport.execute(sessionStore.bearerRequest("POST", "/api/account/2fa/disable", body))
        ensureHttpSuccess(response, 200)
        return JSONObject(response.body).optBoolean("success", true)
    }

    override suspend fun verifyChallenge(challengeToken: String, code: String): MobileSession {
        val body = JSONObject()
            .put("challengeToken", challengeToken)
            .put("code", code)
            .toString()
        val response = transport.execute(
            HttpRequest(
                method = "POST",
                path = "/api/mobile/auth/2fa/verify",
                headers = mapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
                ),
                body = body
            )
        )
        ensureHttpSuccess(response, 200)
        return try {
            val root = JSONObject(response.body)
            val session = root.getJSONObject("session")
            val userJson = root.getJSONObject("user")
            val parsedUser = MobileUser(
                id = userJson.getLong("id"),
                name = userJson.getString("name"),
                email = userJson.getString("email"),
                phone = userJson.optString("phone").takeIf { it.isNotBlank() },
                role = userJson.optString("role", "client"),
                emailVerified = userJson.optBoolean("emailVerified", false),
                isAdmin = userJson.optBoolean("isAdmin", false)
            )
            val sessionObj = MobileSession(
                user = parsedUser,
                accessToken = session.getString("accessToken"),
                expiresAt = session.getString("expiresAt")
            )
            sessionStore.writeAccessToken(sessionObj.accessToken)
            sessionObj
        } catch (error: Exception) {
            throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE, cause = error)
        }
    }
}
