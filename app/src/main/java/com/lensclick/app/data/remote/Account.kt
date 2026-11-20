package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.AccountDataExport
import com.lensclick.app.domain.AccountGateway
import org.json.JSONObject

class HttpAccountGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : AccountGateway {

    override suspend fun exportData(): AccountDataExport {
        val response = transport.execute(bearerRequest("GET", "/api/account/export"))
        ensureHttpSuccess(response, 200)
        return AccountDataExport(rawJson = response.body)
    }

    override suspend fun deleteAccount(password: String, confirmation: String): Boolean {
        val body = JSONObject()
            .put("currentPassword", password)
            .put("confirmation", confirmation)
            .toString()
        val response = transport.execute(bearerRequest("DELETE", "/api/account", body))
        ensureHttpSuccess(response, 200)
        sessionStore.clear()
        return true
    }

    private fun bearerRequest(method: String, path: String, body: String? = null): HttpRequest {
        val token = sessionStore.readAccessToken()
            ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
        val headers = buildMap {
            put("Accept", "application/json")
            put("Authorization", "Bearer $token")
            if (body != null) put("Content-Type", "application/json")
        }
        return HttpRequest(method, path, headers, body)
    }
}
