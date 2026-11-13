package com.lensclick.app.data.remote.auth

data class MobileAuthRequest(
    val email: String,
    val password: String,
    val deviceName: String,
    val appVersion: String
)

data class MobileRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "client",
    val deviceName: String = "Android",
    val appVersion: String = "1.0"
)

data class MobilePhotographerRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val specialty: String,
    val city: String,
    val price: String,
    val bio: String,
    val role: String = "photographer",
    val deviceName: String = "Android",
    val appVersion: String = "1.0"
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