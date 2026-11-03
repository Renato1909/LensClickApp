package com.example.lensclickapp.data.auth

data class MobileUser(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val emailVerified: Boolean,
    val isAdmin: Boolean
)

data class MobileSession(
    val user: MobileUser,
    val accessToken: String,
    val expiresAt: String
)

data class MobileAuthRequest(
    val email: String,
    val password: String,
    val deviceName: String,
    val appVersion: String
)

data class HttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String = ""
)

enum class MobileAuthErrorCode {
    INVALID_INPUT,
    UNAUTHENTICATED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    RATE_LIMITED,
    SERVER,
    NETWORK,
    MALFORMED_RESPONSE,
    STORAGE
}

class MobileAuthException(
    val code: MobileAuthErrorCode,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(code.name, cause)
