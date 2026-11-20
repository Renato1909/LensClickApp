package com.lensclick.app.domain

data class TwoFactorStatus(
    val enabled: Boolean,
    val method: String = "totp"
)

data class TwoFactorSetup(
    val secret: String,
    val otpauthUrl: String,
    val recoveryCodes: List<String>
)

data class TwoFactorChallenge(
    val challengeToken: String,
    val email: String,
    val expiresAt: String
)

interface TwoFactorGateway {
    suspend fun getStatus(): TwoFactorStatus
    suspend fun setup(): TwoFactorSetup
    suspend fun enable(code: String): Boolean
    suspend fun disable(password: String, code: String? = null): Boolean
    suspend fun verifyChallenge(challengeToken: String, code: String): MobileSession
}
