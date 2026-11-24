package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.data.remote.auth.parseMobileUser
import com.lensclick.app.domain.AccountDataExport
import com.lensclick.app.domain.AccountGateway
import com.lensclick.app.domain.MobileUser
import org.json.JSONObject

class HttpAccountGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : AccountGateway {

    override suspend fun enableProfessionalAccount(phone: String): MobileUser {
        val me = transport.execute(sessionStore.bearerRequest("GET", "/api/mobile/auth/me"))
        ensureHttpSuccess(me, 200)
        val user = JSONObject(me.body).getJSONObject("user")
        val payload = JSONObject()
            .put("name", user.getString("name"))
            .put("email", user.getString("email"))
            .put("phone", phone)
            .put("role", "photographer")
            .put("age", user.opt("age") ?: JSONObject.NULL)
            .put("gender", user.optString("gender", "other"))
            .put("profilePic", user.optString("profilePic", ""))
        val response = transport.execute(sessionStore.bearerRequest("PUT", "/api/profile", payload.toString()))
        ensureHttpSuccess(response, 200)
        val updated = parseMobileUser(JSONObject(response.body).getJSONObject("user"))
        if (updated.id != user.getLong("id") || updated.role != "photographer") {
            throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE)
        }
        return updated
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val body = JSONObject()
            .put("currentPassword", currentPassword)
            .put("newPassword", newPassword)
            .toString()
        val response = transport.execute(sessionStore.bearerRequest("PUT", "/api/account/password", body))
        ensureHttpSuccess(response, 200)
    }

    override suspend fun exportData(): AccountDataExport {
        val response = transport.execute(sessionStore.bearerRequest("GET", "/api/account/export"))
        ensureHttpSuccess(response, 200)
        return AccountDataExport(rawJson = response.body)
    }

    override suspend fun deleteAccount(password: String, confirmation: String): Boolean {
        val body = JSONObject()
            .put("currentPassword", password)
            .put("confirmation", confirmation)
            .toString()
        val response = transport.execute(sessionStore.bearerRequest("DELETE", "/api/account", body))
        ensureHttpSuccess(response, 200)
        sessionStore.clear()
        return true
    }
}
