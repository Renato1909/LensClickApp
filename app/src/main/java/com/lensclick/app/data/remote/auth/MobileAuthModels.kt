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
    val body: String? = null,
    val bodyBytes: ByteArray? = null
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

internal fun httpErrorCodeFor(statusCode: Int): MobileAuthErrorCode = when (statusCode) {
    400, 413, 415 -> MobileAuthErrorCode.INVALID_INPUT
    401 -> MobileAuthErrorCode.UNAUTHENTICATED
    403 -> MobileAuthErrorCode.FORBIDDEN
    404 -> MobileAuthErrorCode.NOT_FOUND
    409 -> MobileAuthErrorCode.CONFLICT
    429 -> MobileAuthErrorCode.RATE_LIMITED
    in 500..599 -> MobileAuthErrorCode.SERVER
    else -> MobileAuthErrorCode.SERVER
}

internal fun ensureHttpSuccess(response: HttpResponse, vararg expectedStatuses: Int) {
    if (response.statusCode in expectedStatuses) return
    throw MobileAuthException(code = httpErrorCodeFor(response.statusCode), statusCode = response.statusCode)
}