package com.lensclick.app.ui

import androidx.lifecycle.ViewModelStore
import com.lensclick.app.data.local.FakeLensClickDao
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.local.User
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthApi
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.MobileAuthRequest
import com.lensclick.app.data.remote.auth.MobileLoginResult
import com.lensclick.app.data.remote.auth.MobilePhotographerRegisterRequest
import com.lensclick.app.data.remote.auth.MobileRegisterRequest
import com.lensclick.app.domain.AccountDataExport
import com.lensclick.app.domain.AccountGateway
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.BudgetRequestsGateway
import com.lensclick.app.domain.Conversation
import com.lensclick.app.domain.ConversationsGateway
import com.lensclick.app.domain.Message
import com.lensclick.app.domain.MobileSession
import com.lensclick.app.domain.MobileUser
import com.lensclick.app.domain.PublicPhotographerNotFoundException
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.domain.PublicReviews
import com.lensclick.app.domain.Review
import com.lensclick.app.domain.ReviewSummary
import com.lensclick.app.domain.ReviewsGateway
import com.lensclick.app.domain.TwoFactorGateway
import com.lensclick.app.domain.TwoFactorSetup
import com.lensclick.app.domain.TwoFactorStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LensClickViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private lateinit var dao: FakeLensClickDao
    private lateinit var viewModel: LensClickViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        dao = FakeLensClickDao()
        viewModel = LensClickViewModel(LensClickRepository(dao))
        store.put("test", viewModel)
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun successfulLoginExposesUserAndLogoutClearsSession() = runTest(dispatcher) {
        advanceUntilIdle()
        val store = InMemorySessionStore()
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store))
        viewModel.login("user@example.com", "Secret123")
        advanceUntilIdle()

        assertNotNull(viewModel.currentUser.value)
        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
        viewModel.logout()
        advanceUntilIdle()
        assertNull(viewModel.currentUser.value)
        assertEquals(AuthenticationState.LogoutCompleted, viewModel.authentication.value)
    }

    @Test
    fun failedLoginExposesDeterministicError() = runTest(dispatcher) {
        val store = InMemorySessionStore()
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store, 401))
        advanceUntilIdle()
        viewModel.login("user@example.com", "incorrect")
        advanceUntilIdle()

        assertEquals(AuthenticationState.Error(MobileAuthErrorCode.UNAUTHENTICATED), viewModel.authentication.value)
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun twoFactorChallengeRequiredOnLogin() = runTest(dispatcher) {
        val store = InMemorySessionStore()
        val repository = authRepository(store, status = 202)
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = repository)
        advanceUntilIdle()
        viewModel.login("user@example.com", "Secret123")
        advanceUntilIdle()

        assertTrue(viewModel.authentication.value is AuthenticationState.TwoFactorRequired)
        val challenge = viewModel.authentication.value as AuthenticationState.TwoFactorRequired
        assertEquals("user@example.com", challenge.email)
    }

    @Test
    fun verifyTwoFactorChallengeAuthenticatesUser() = runTest(dispatcher) {
        val sessionStore = InMemorySessionStore()
        val gateway = object : TwoFactorGateway {
            override suspend fun getStatus(): TwoFactorStatus = TwoFactorStatus(true)
            override suspend fun setup(): TwoFactorSetup = TwoFactorSetup("secret", "url", emptyList())
            override suspend fun enable(code: String): Boolean = true
            override suspend fun disable(password: String, code: String?): Boolean = true
            override suspend fun verifyChallenge(challengeToken: String, code: String): MobileSession =
                MobileSession(
                    user = MobileUser(1, "2FA User", "2fa@example.com", null, "client", true, false),
                    accessToken = "2fa_bearer",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
        }
        viewModel = LensClickViewModel(
            LensClickRepository(dao),
            mobileAuthRepository = authRepository(sessionStore),
            twoFactorGateway = gateway
        )
        advanceUntilIdle()

        viewModel.verifyTwoFactorChallenge("token_123", "123456")
        advanceUntilIdle()

        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
        assertEquals("2fa@example.com", viewModel.currentUser.value?.email)
    }

    @Test
    fun conversationsAndMessagesIntegration() = runTest(dispatcher) {
        val conversation = Conversation(10, "Fotógrafo", "Olá", "2026-11-15", 1)
        val message = Message(100, 1, "Oi!", "2026-11-15", true)
        val gateway = object : ConversationsGateway {
            override suspend fun list(): List<Conversation> = listOf(conversation)
            override suspend fun getMessages(conversationId: Long): List<Message> = listOf(message)
            override suspend fun sendMessage(conversationId: Long, text: String): Message = message
            override suspend fun startConversation(photographerSlug: String, text: String): Conversation = conversation
            override suspend fun markAsRead(conversationId: Long) {}
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), conversationsGateway = gateway)
        viewModel.loadConversations()
        advanceUntilIdle()
        assertEquals(1, viewModel.conversations.value.size)

        viewModel.loadMessages(10)
        advanceUntilIdle()
        assertEquals(1, viewModel.messages.value.size)
        assertEquals("Oi!", viewModel.messages.value[0].text)
    }

    @Test
    fun reviewsIntegration() = runTest(dispatcher) {
        val reviews = PublicReviews(
            summary = ReviewSummary(1, 5.0),
            reviews = listOf(Review(1, "Cliente", 5, "Excelente!", "2026-11-15"))
        )
        val gateway = object : ReviewsGateway {
            override suspend fun getReviews(photographerSlug: String): PublicReviews = reviews
            override suspend fun submitReview(photographerSlug: String, rating: Int, comment: String): Boolean = true
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), reviewsGateway = gateway)
        viewModel.loadReviews("fotografo")
        advanceUntilIdle()
        assertEquals(1, viewModel.publicReviews.value?.summary?.total)
    }

    @Test
    fun accountLgpdExportAndDelete() = runTest(dispatcher) {
        val gateway = object : AccountGateway {
            override suspend fun exportData(): AccountDataExport = AccountDataExport("""{"user":"Test"}""")
            override suspend fun deleteAccount(password: String, confirmation: String): Boolean = true
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), accountGateway = gateway)

        var exportedJson: String? = null
        viewModel.exportAccountData(
            onSuccess = { exportedJson = it },
            onError = {}
        )
        advanceUntilIdle()
        assertNotNull(exportedJson)
        assertTrue(exportedJson!!.contains("Test"))

        var deleted = false
        viewModel.deleteAccount(
            password = "password",
            onSuccess = { deleted = true },
            onError = {}
        )
        advanceUntilIdle()
        assertTrue(deleted)
        assertEquals(AuthenticationState.SignedOut, viewModel.authentication.value)
    }

    private fun authRepository(store: InMemorySessionStore, status: Int = 200): MobileAuthRepository =
        MobileAuthRepository(object : MobileAuthApi {
            override suspend fun login(request: MobileAuthRequest): MobileLoginResult {
                if (status == 202) {
                    return MobileLoginResult.TwoFactorRequired("challenge_token_real", "2099-01-01T00:00:00Z")
                }
                if (status != 200) throw MobileAuthException(
                    code = MobileAuthErrorCode.UNAUTHENTICATED,
                    statusCode = status
                )
                return MobileLoginResult.Success(
                    MobileSession(
                        user = MobileUser(42, "Pessoa", "user@example.com", null, "client", true, false),
                        accessToken = "access-token",
                        expiresAt = "2099-01-01T00:00:00Z"
                    )
                )
            }

            override suspend fun register(request: MobileRegisterRequest): MobileSession {
                if (status != 200) throw MobileAuthException(
                    code = if (status == 409) MobileAuthErrorCode.CONFLICT else MobileAuthErrorCode.INVALID_INPUT,
                    statusCode = status
                )
                return MobileSession(
                    user = MobileUser(42, request.name, request.email, null, request.role, true, false),
                    accessToken = "access-token",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
            }

            override suspend fun registerPhotographer(request: MobilePhotographerRegisterRequest): MobileSession {
                if (status != 200) throw MobileAuthException(
                    code = if (status == 409) MobileAuthErrorCode.CONFLICT else MobileAuthErrorCode.INVALID_INPUT,
                    statusCode = status
                )
                return MobileSession(
                    user = MobileUser(99, request.name, request.email, null, request.role, true, false),
                    accessToken = "pro-access-token",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
            }

            override suspend fun currentUser(accessToken: String): MobileUser {
                if (status != 200) throw MobileAuthException(
                    code = MobileAuthErrorCode.UNAUTHENTICATED,
                    statusCode = status
                )
                return MobileUser(42, "Pessoa", "user@example.com", null, "client", true, false)
            }

            override suspend fun logout(accessToken: String) = Unit
        }, store)
}
