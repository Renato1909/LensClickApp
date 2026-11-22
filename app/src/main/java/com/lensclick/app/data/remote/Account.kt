package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpTransport
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
