package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore

internal fun SessionStore.bearerRequest(
    method: String,
    path: String,
    body: String? = null,
    bodyBytes: ByteArray? = null,
    contentType: String = "application/json"
): HttpRequest {
    val token = readAccessToken()
        ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
    val headers = buildMap {
        put("Accept", "application/json")
        put("Authorization", "Bearer $token")
        if (body != null || bodyBytes != null) put("Content-Type", contentType)
    }
    return HttpRequest(method, path, headers, body, bodyBytes)
}
