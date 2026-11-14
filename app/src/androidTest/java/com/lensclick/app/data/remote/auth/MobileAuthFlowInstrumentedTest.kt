package com.lensclick.app.data.remote.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lensclick.app.domain.MobileSession
import com.lensclick.app.domain.MobileUser
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Fluxos de autenticação no runtime Android usando o Keystore real como
 * armazenamento de sessão e uma API falsa (sem rede/endpoint inventado):
 * restauração, logout, troca de conta e estados de rede.
 */
@RunWith(AndroidJUnit4::class)
class MobileAuthFlowInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val keyAlias = "lensclick.test.flow.session"
    private val store = AndroidKeystoreSessionStore(context, keyAlias)

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun restoreReturnsNullWithoutToken() = runBlocking {
        val repository = MobileAuthRepository(FakeFlowApi(), store)
        assertNull(repository.restore())
    }

    @Test
    fun loginThenRestoreReturnsUserFromKeystoreToken() = runBlocking {
        val repository = MobileAuthRepository(FakeFlowApi(), store)
        repository.login("user@example.com", "Secret123", "Android", "1.0")

        val user = repository.restore()

        assertEquals("user@example.com", user?.email)
    }

    @Test
    fun logoutClearsKeystoreToken() = runBlocking {
        val repository = MobileAuthRepository(FakeFlowApi(), store)
        repository.login("user@example.com", "Secret123", "Android", "1.0")

        repository.logout()

        assertNull(store.readAccessToken())
        assertNull(repository.restore())
    }

    @Test
    fun accountSwitchReplacesKeystoreToken() = runBlocking {
        val repository = MobileAuthRepository(FakeFlowApi(), store)

        repository.login("a@example.com", "Secret123", "Android", "1.0")
        assertEquals("token-a@example.com", store.readAccessToken())

        repository.login("b@example.com", "Secret123", "Android", "1.0")
        assertEquals("token-b@example.com", store.readAccessToken())
        assertEquals("b@example.com", repository.restore()?.email)
    }

    @Test
    fun networkFailureOnRestoreKeepsTokenAndPropagates() = runBlocking {
        store.writeAccessToken("kept-token")
        val repository = MobileAuthRepository(NetworkFailingFlowApi(), store)

        try {
            repository.restore()
            throw AssertionError("Expected NETWORK error")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.NETWORK, error.code)
        }

        assertEquals("kept-token", store.readAccessToken())
    }

    @Test
    fun logoutClearsTokenEvenOnNetworkFailure() = runBlocking {
        store.writeAccessToken("kept-token")
        val repository = MobileAuthRepository(NetworkFailingFlowApi(), store)

        try {
            repository.logout()
            throw AssertionError("Expected NETWORK error")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.NETWORK, error.code)
        }

        assertNull(store.readAccessToken())
    }
}

private class FakeFlowApi : MobileAuthApi {
    override suspend fun login(request: MobileAuthRequest): MobileSession =
        MobileSession(
            user = MobileUser(1, "Conta", request.email, null, "client", true, false),
            accessToken = "token-${request.email}",
            expiresAt = "2099-01-01T00:00:00Z"
        )

    override suspend fun register(request: MobileRegisterRequest): MobileSession = error("Unused")

    override suspend fun registerPhotographer(request: MobilePhotographerRegisterRequest): MobileSession = error("Unused")

    override suspend fun currentUser(accessToken: String): MobileUser {
        val email = accessToken.removePrefix("token-")
        return MobileUser(1, "Conta", email, null, "client", true, false)
    }

    override suspend fun logout(accessToken: String) = Unit
}

private class NetworkFailingFlowApi : MobileAuthApi {
    override suspend fun login(request: MobileAuthRequest): MobileSession =
        throw MobileAuthException(MobileAuthErrorCode.NETWORK)

    override suspend fun register(request: MobileRegisterRequest): MobileSession =
        throw MobileAuthException(MobileAuthErrorCode.NETWORK)

    override suspend fun registerPhotographer(request: MobilePhotographerRegisterRequest): MobileSession =
        throw MobileAuthException(MobileAuthErrorCode.NETWORK)

    override suspend fun currentUser(accessToken: String): MobileUser =
        throw MobileAuthException(MobileAuthErrorCode.NETWORK)

    override suspend fun logout(accessToken: String) =
        throw MobileAuthException(MobileAuthErrorCode.NETWORK)
}