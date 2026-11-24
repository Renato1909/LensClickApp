package com.lensclick.app.ui

import androidx.lifecycle.ViewModelStore
import com.lensclick.app.data.local.FakeLensClickDao
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.local.User
import com.lensclick.app.data.remote.HttpAccountGateway
import com.lensclick.app.data.remote.HttpProfessionalProfileGateway
import com.lensclick.app.data.remote.auth.HttpMobileAuthApi
import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthApi
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.MobileAuthRequest
import com.lensclick.app.data.remote.auth.MobileLoginResult
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
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.PublicPhotographer
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
import org.json.JSONObject
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
    fun verifyTwoFactorChallengeKeepsTheChallengeAfterInvalidCode() = runTest(dispatcher) {
        val sessionStore = InMemorySessionStore()
        val submitted = mutableListOf<Pair<String, String>>()
        val gateway = object : TwoFactorGateway {
            override suspend fun getStatus(): TwoFactorStatus = TwoFactorStatus(true)
            override suspend fun setup(): TwoFactorSetup = TwoFactorSetup("secret", "url", emptyList())
            override suspend fun enable(code: String): Boolean = true
            override suspend fun disable(password: String, code: String): Boolean = true
            override suspend fun verifyChallenge(challengeToken: String, code: String): MobileSession {
                submitted += challengeToken to code
                if (code != "ABCD-EFGH") throw MobileAuthException(MobileAuthErrorCode.INVALID_INPUT)
                return MobileSession(
                    user = MobileUser(1, "2FA User", "2fa@example.com", null, "client", true, false),
                    accessToken = "2fa_bearer",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
            }
        }
        viewModel = LensClickViewModel(
            LensClickRepository(dao),
            mobileAuthRepository = authRepository(sessionStore, status = 202),
            twoFactorGateway = gateway
        )
        advanceUntilIdle()

        viewModel.verifyTwoFactorChallenge("123456")
        assertTrue(submitted.isEmpty())
        viewModel.login("user@example.com", "Secret123")
        advanceUntilIdle()
        viewModel.verifyTwoFactorChallenge("123456")
        advanceUntilIdle()

        val challenge = viewModel.authentication.value as AuthenticationState.TwoFactorRequired
        assertEquals(MobileAuthErrorCode.INVALID_INPUT, challenge.error)
        assertFalse(challenge.verifying)
        assertNull(viewModel.currentUser.value)
        assertNull(sessionStore.readAccessToken())

        viewModel.verifyTwoFactorChallenge("ABCD-EFGH")
        advanceUntilIdle()

        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
        assertEquals("2fa@example.com", viewModel.currentUser.value?.email)
        assertEquals(listOf("challenge_token_real" to "123456", "challenge_token_real" to "ABCD-EFGH"), submitted)
    }

    @Test
    fun disableTwoFactorClearsTheRevokedSession() = runTest(dispatcher) {
        val sessionStore = InMemorySessionStore()
        val gateway = object : TwoFactorGateway {
            override suspend fun getStatus() = TwoFactorStatus(true)
            override suspend fun setup() = error("Unexpected setup")
            override suspend fun enable(code: String) = error("Unexpected enable")
            override suspend fun verifyChallenge(challengeToken: String, code: String) = error("Unexpected verification")
            override suspend fun disable(password: String, code: String): Boolean {
                assertEquals("Secret123", password)
                assertEquals("ABCD-EFGH", code)
                return true
            }
        }
        viewModel = LensClickViewModel(
            LensClickRepository(dao),
            mobileAuthRepository = authRepository(sessionStore),
            twoFactorGateway = gateway
        )
        viewModel.login("user@example.com", "Secret123")
        advanceUntilIdle()
        assertNotNull(sessionStore.readAccessToken())

        var completed = false
        viewModel.disableTwoFactor("Secret123", "ABCD-EFGH") { error ->
            assertNull(error)
            completed = true
        }
        advanceUntilIdle()

        assertTrue(completed)
        assertEquals(AuthenticationState.LogoutCompleted, viewModel.authentication.value)
        assertNull(viewModel.currentUser.value)
        assertNull(sessionStore.readAccessToken())
    }

    @Test
    fun photographerRegistrationRecoversFromDraftFailureWithoutRegisteringAgain() = runTest(dispatcher) {
        val session = InMemorySessionStore()
        val requests = mutableListOf<HttpRequest>()
        val transport = object : HttpTransport {
            var role = "client"
            var saves = 0
            fun account() = JSONObject().put("id", 21).put("name", "Pessoa de teste")
                .put("email", "foto@example.com").put("role", role)
                .put("age", 34).put("gender", "other").put("profilePic", "/api/profile-picture/21?v=1")

            override suspend fun execute(request: HttpRequest): HttpResponse {
                requests.add(request)
                return when (request.method to request.path) {
                    "POST" to "/api/mobile/auth/register" -> HttpResponse(201, body = JSONObject()
                        .put("user", account())
                        .put("session", JSONObject().put("accessToken", "session").put("expiresAt", "2099-01-01T00:00:00Z"))
                        .toString())
                    "GET" to "/api/mobile/auth/me" -> HttpResponse(200, body = JSONObject().put("user", account()).toString())
                    "PUT" to "/api/profile" -> {
                        role = "photographer"
                        HttpResponse(200, body = JSONObject().put("user", account()).toString())
                    }
                    "GET" to "/api/account/professional-profile" -> HttpResponse(200, body = """{"profile":{"slug":"foto-21"}}""")
                    "PUT" to "/api/account/professional-profile" -> {
                        saves++
                        if (saves == 1) HttpResponse(503)
                        else HttpResponse(200, body = """{"profile":{"slug":"foto-21"}}""")
                    }
                    else -> error("Unexpected test request ${request.method} ${request.path}")
                }
            }
        }
        viewModel = LensClickViewModel(
            LensClickRepository(dao),
            mobileAuthRepository = MobileAuthRepository(HttpMobileAuthApi(transport), session),
            professionalProfileGateway = HttpProfessionalProfileGateway(transport, session),
            accountGateway = HttpAccountGateway(transport, session)
        )
        val draft = ProfessionalProfile(bio = "Retratos e eventos", specialties = listOf("portrait"), city = "São Paulo", startingPriceCents = 120050)
        var completed: Boolean? = null
        viewModel.registerPhotographer("Pessoa de teste", "foto@example.com", "Senha123", "11900000000", draft) { completed = it }
        advanceUntilIdle()
        assertEquals(false, completed)
        assertTrue(viewModel.finishingPhotographerRegistration.value)
        assertNull(viewModel.currentUser.value)
        viewModel.registerPhotographer("Pessoa de teste", "foto@example.com", "", "11900000000", draft) { completed = it }
        advanceUntilIdle()

        assertEquals(true, completed)
        assertEquals("photographer", viewModel.currentUser.value?.role)
        assertEquals(1, requests.count { it.path == "/api/mobile/auth/register" })
        val update = JSONObject(requests.single { it.method == "PUT" && it.path == "/api/profile" }.body!!)
        assertEquals(34, update.getInt("age"))
        assertEquals("/api/profile-picture/21?v=1", update.getString("profilePic"))
        val profile = JSONObject(requests.last { it.method == "PUT" }.body!!)
        assertEquals("portrait", profile.getJSONArray("specialties").getString(0))
        assertEquals(120050, profile.getInt("startingPriceCents"))
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
        assertEquals(10L, viewModel.chat.value?.conversationId)
        assertEquals(listOf(message), viewModel.chat.value?.messages)
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
            override suspend fun requestVerification(photographerSlug: String, message: String) = Unit
            override suspend fun listReviewRequests() = emptyList<com.lensclick.app.domain.ReviewRequest>()
            override suspend fun resolveReviewRequest(id: Long, confirm: Boolean) = Unit
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), reviewsGateway = gateway)
        viewModel.loadReviews("fotografo")
        advanceUntilIdle()
        assertEquals(1, viewModel.publicReviews.value?.data?.summary?.total)
    }

    @Test
    fun photographerRankingKeepsServerOrderAndRetriesTheFailedPage() = runTest(dispatcher) {
        fun photographer(slug: String, rating: Double?, count: Int) = PublicPhotographer(
            slug = slug, name = slug, city = "", state = "", specialties = emptyList(),
            startingPriceCents = null, ratingAverage = rating, reviewCount = count
        )
        val requests = mutableListOf<Int>()
        val gateway = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int): PublicPhotographerPage =
                error("Ranking must use the sorted endpoint")

            override suspend fun listTopRated(page: Int): PublicPhotographerPage {
                requests += page
                val photographers = when {
                    page == 1 -> listOf(
                        photographer("primeira-colocada", 5.0, 8),
                        photographer("segundo-colocado", 5.0, 3),
                        photographer("feira-amostra", 5.0, 1),
                        photographer(DemoPhotographerSlug, 5.0, 1)
                    )
                    page == 2 && requests.count { it == 2 } == 1 -> throw java.io.IOException()
                    page == 2 -> listOf(
                        photographer("segundo-colocado", 5.0, 3),
                        photographer("terceira-colocada", 4.75, 9),
                        photographer("perfil-sem-nota", null, 0)
                    )
                    else -> error("Ranking should stop before the remaining unrated pages")
                }
                return PublicPhotographerPage(photographers, page, total = 30, totalPages = 3)
            }
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), publicPhotographers = gateway)
        val excluded = setOf("feira-amostra")
        viewModel.loadPhotographerRanking(excluded)
        advanceUntilIdle()
        val firstPage = viewModel.photographerRanking.value
        assertEquals(listOf("primeira-colocada", "segundo-colocado"), firstPage.photographers.map { it.slug })
        assertEquals(2, firstPage.nextPage)

        viewModel.loadPhotographerRanking(excluded)
        advanceUntilIdle()
        assertEquals(firstPage.copy(failed = true), viewModel.photographerRanking.value)
        viewModel.loadPhotographerRanking(excluded)
        advanceUntilIdle()
        val finalPage = viewModel.photographerRanking.value
        assertEquals(
            listOf("primeira-colocada", "segundo-colocado", "terceira-colocada"),
            finalPage.photographers.map { it.slug }
        )
        assertNull(finalPage.nextPage)
        viewModel.loadPhotographerRanking(excluded)
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 2), requests)
    }

    @Test
    fun accountLgpdExportAndDelete() = runTest(dispatcher) {
        val gateway = object : AccountGateway {
            override suspend fun enableProfessionalAccount(phone: String): MobileUser = error("Unused")
            override suspend fun changePassword(currentPassword: String, newPassword: String) = Unit
            override suspend fun exportData(): AccountDataExport = AccountDataExport("""{"user":"Test"}""")
            override suspend fun deleteAccount(password: String, confirmation: String): Boolean = true
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), accountGateway = gateway)

        assertTrue(viewModel.fetchAccountExport().contains("Test"))

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
