package com.example.lensclickapp.data.auth

class MobileAuthRepository(
    private val api: MobileAuthApi,
    private val sessionStore: SessionStore
) {
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        deviceName: String,
        appVersion: String
    ): MobileSession {
        val session = api.register(
            MobileRegistrationRequest(
                name = name.trim(),
                email = email.trim().lowercase(),
                phone = phone.trim(),
                password = password,
                deviceName = deviceName,
                appVersion = appVersion
            )
        )
        sessionStore.writeAccessToken(session.accessToken)
        return session
    }

    suspend fun login(
        email: String,
        password: String,
        deviceName: String,
        appVersion: String
    ): MobileSession {
        val session = api.login(
            MobileAuthRequest(
                email = email.trim().lowercase(),
                password = password,
                deviceName = deviceName,
                appVersion = appVersion
            )
        )
        try {
            sessionStore.writeAccessToken(session.accessToken)
        } catch (error: MobileAuthException) {
            throw error
        }
        return session
    }

    suspend fun restore(): MobileUser? {
        val accessToken = sessionStore.readAccessToken() ?: return null
        return try {
            api.currentUser(accessToken)
        } catch (error: MobileAuthException) {
            if (error.code == MobileAuthErrorCode.UNAUTHENTICATED) sessionStore.clear()
            throw error
        }
    }

    suspend fun logout() {
        val accessToken = sessionStore.readAccessToken()
        try {
            if (accessToken != null) api.logout(accessToken)
        } finally {
            sessionStore.clear()
        }
    }
}
