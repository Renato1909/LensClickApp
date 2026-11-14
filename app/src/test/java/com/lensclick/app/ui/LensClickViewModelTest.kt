package com.lensclick.app.ui

import androidx.lifecycle.ViewModelStore
import com.lensclick.app.data.local.FakeLensClickDao
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographerNotFoundException
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.data.local.User
import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthApi
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.data.remote.auth.MobileAuthRequest
import com.lensclick.app.data.remote.auth.MobilePhotographerRegisterRequest
import com.lensclick.app.data.remote.auth.MobileRegisterRequest
import com.lensclick.app.domain.MobileSession
import com.lensclick.app.domain.MobileUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    fun revokedSessionBecomesExpiredAndClearsToken() = runTest(dispatcher) {
        val store = InMemorySessionStore().apply { writeAccessToken("revoked") }
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store, 401))
        advanceUntilIdle()
        assertEquals(AuthenticationState.SessionExpired, viewModel.authentication.value)
        assertNull(viewModel.currentUser.value)
        assertNull(store.readAccessToken())
    }

    @Test
    fun loadingIsExposedDuringRemoteAuthentication() = runTest(dispatcher) {
        val store = InMemorySessionStore()
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store))
        viewModel.login("user@example.com", "Secret123")
        assertEquals(AuthenticationState.Loading, viewModel.authentication.value)
        advanceUntilIdle()
        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
    }

    @Test
    fun loginWaitsForRestoreBeforeReplacingTheSession() = runTest(dispatcher) {
        val store = InMemorySessionStore().apply { writeAccessToken("restore-token") }
        val restoreRelease = CompletableDeferred<Unit>()
        var loginStarted = false
        val api = object : MobileAuthApi {
            override suspend fun login(request: MobileAuthRequest): MobileSession {
                loginStarted = true
                return MobileSession(
                    user = MobileUser(2, "Conta B", "b@example.com", null, "client", true, false),
                    accessToken = "account-b-token",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
            }

            override suspend fun currentUser(accessToken: String): MobileUser {
                restoreRelease.await()
                return MobileUser(1, "Conta A", "a@example.com", null, "client", true, false)
            }

            override suspend fun register(request: MobileRegisterRequest): MobileSession = error("Unused")

            override suspend fun registerPhotographer(request: MobilePhotographerRegisterRequest): MobileSession = error("Unused")

            override suspend fun logout(accessToken: String) = Unit
        }
        viewModel = LensClickViewModel(
            LensClickRepository(dao),
            mobileAuthRepository = MobileAuthRepository(api, store)
        )

        advanceUntilIdle()
        viewModel.login("b@example.com", "Secret123")
        advanceUntilIdle()
        assertFalse(loginStarted)

        restoreRelease.complete(Unit)
        advanceUntilIdle()
        assertTrue(loginStarted)
        assertEquals("b@example.com", viewModel.currentUser.value?.email)
    }

    @Test
    fun budgetCompletionAndObservedStateFollowStoredRequest() = runTest(dispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.budgets.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.photographers.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.photographers.value.isNotEmpty())
        val originalCount = viewModel.budgets.value.size
        var done = false
        viewModel.createBudget("Ensaio", "20/09/2026", "Recife", "2 horas", "Retratos", "") {
            assertEquals("Ensaio", dao.budgetRows.value.last().title)
            done = true
        }
        assertFalse(done)
        advanceUntilIdle()

        assertTrue(done)
        assertEquals(originalCount + 1, viewModel.budgets.value.size)
        assertEquals("Ensaio", viewModel.budgets.value.last().title)
    }

    @Test
    fun publicSearchUsesServerFiltersAndExposesAnEmptyPage() = runTest(dispatcher) {
        var requested: Triple<String, String?, Int>? = null
        val gateway = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int): PublicPhotographerPage {
                requested = Triple(city, specialty, page)
                return PublicPhotographerPage(emptyList(), page, 0, 0)
            }

            override suspend fun get(slug: String): PublicPhotographerProfile = error("Unused")
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), gateway)
        store.put("test", viewModel)

        viewModel.searchPublicPhotographers("Recife", "wedding", 2)
        advanceUntilIdle()

        assertEquals(Triple("Recife", "wedding", 2), requested)
        assertEquals(PublicSearchState.Empty(PublicPhotographerPage(emptyList(), 2, 0, 0)), viewModel.publicSearch.value)
    }

    @Test
    fun publicProfileDistinguishesNotFoundOrUnpublished() = runTest(dispatcher) {
        val gateway = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int) =
                PublicPhotographerPage(emptyList(), page, 0, 0)

            override suspend fun get(slug: String): PublicPhotographerProfile =
                throw PublicPhotographerNotFoundException(slug)
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), gateway)
        viewModel.loadPublicPhotographer("missing-1")
        advanceUntilIdle()

        assertEquals(PublicProfileState.NotFoundOrUnpublished, viewModel.publicProfile.value)
    }

    @Test
    fun publicProfileExposesUnexpectedGatewayErrors() = runTest(dispatcher) {
        val gateway = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int) =
                PublicPhotographerPage(emptyList(), page, 0, 0)

            override suspend fun get(slug: String): PublicPhotographerProfile =
                throw java.io.IOException("network unavailable")
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), gateway)
        viewModel.loadPublicPhotographer("ana-12")
        advanceUntilIdle()

        assertEquals(PublicProfileState.Error, viewModel.publicProfile.value)
    }

    @Test
    fun publicProfileTreatsUnpublishedResponseAsUnavailable() = runTest(dispatcher) {
        val gateway = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int) =
                PublicPhotographerPage(emptyList(), page, 0, 0)

            override suspend fun get(slug: String): PublicPhotographerProfile =
                PublicPhotographerProfile(
                    profile = com.lensclick.app.domain.PublicPhotographer(
                        slug = slug,
                        name = "Rascunho",
                        city = "",
                        state = "",
                        specialties = emptyList(),
                        startingPriceCents = null,
                        ratingAverage = null,
                        reviewCount = 0
                    ),
                    bio = "",
                    serviceRadiusKm = null,
                    availability = "unavailable",
                    instagram = "",
                    website = "",
                    profilePic = "",
                    contacts = com.lensclick.app.domain.PublicPhotographerContacts("", ""),
                    portfolio = emptyList(),
                    isPublished = false
                )
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), gateway)
        viewModel.loadPublicPhotographer("draft-1")
        advanceUntilIdle()

        assertEquals(PublicProfileState.NotFoundOrUnpublished, viewModel.publicProfile.value)
    }

    @Test
    fun successfulRegisterUpdatesUserAndInvokesCallback() = runTest(dispatcher) {
        val store = InMemorySessionStore()
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store))
        advanceUntilIdle()

        var doneCalled = false
        var success = false
        viewModel.register("Novo Usuario", "novo@example.com", "Secret123") { result ->
            doneCalled = true
            success = result
        }
        advanceUntilIdle()

        assertTrue(doneCalled)
        assertTrue(success)
        assertNotNull(viewModel.currentUser.value)
        assertEquals("novo@example.com", viewModel.currentUser.value?.email)
        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
    }

    @Test
    fun successfulPhotographerRegisterUpdatesUserWithRole() = runTest(dispatcher) {
        val store = InMemorySessionStore()
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store))
        advanceUntilIdle()

        var doneCalled = false
        var success = false
        viewModel.registerPhotographer("Pro Foto", "pro@example.com", "Secret123", "Casamento", "Recife", "1000", "Bio") { result ->
            doneCalled = true
            success = result
        }
        advanceUntilIdle()

        assertTrue(doneCalled)
        assertTrue(success)
        assertNotNull(viewModel.currentUser.value)
        assertEquals("photographer", viewModel.currentUser.value?.role)
        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
    }

    @Test
    fun failedRegisterExposesErrorAndReturnsFalseInCallback() = runTest(dispatcher) {
        val store = InMemorySessionStore()
        viewModel = LensClickViewModel(LensClickRepository(dao), mobileAuthRepository = authRepository(store, status = 409))
        advanceUntilIdle()

        var doneCalled = false
        var success = true
        viewModel.register("Existente", "existente@example.com", "Secret123") { result ->
            doneCalled = true
            success = result
        }
        advanceUntilIdle()

        assertTrue(doneCalled)
        assertFalse(success)
        assertNull(viewModel.currentUser.value)
        assertEquals(AuthenticationState.Error(MobileAuthErrorCode.CONFLICT), viewModel.authentication.value)
    }

    @Test
    fun publicSearchExposesErrorWhenGatewayFails() = runTest(dispatcher) {
        val gateway = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int): PublicPhotographerPage =
                throw java.io.IOException("network unavailable")

            override suspend fun get(slug: String): PublicPhotographerProfile = error("Unused")
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), gateway)
        store.put("test", viewModel)

        viewModel.searchPublicPhotographers("Recife", null, 1)
        advanceUntilIdle()

        assertEquals(PublicSearchState.Error, viewModel.publicSearch.value)
    }

    @Test
    fun accountSwitchReplacesCurrentUserAfterLogout() = runTest(dispatcher) {
        val sessionStore = InMemorySessionStore()
        val api = object : MobileAuthApi {
            override suspend fun login(request: MobileAuthRequest): MobileSession {
                val id = if (request.email == "a@example.com") 1L else 2L
                return MobileSession(
                    user = MobileUser(id, "Conta", request.email, null, "client", true, false),
                    accessToken = "token-${request.email}",
                    expiresAt = "2099-01-01T00:00:00Z"
                )
            }

            override suspend fun currentUser(accessToken: String): MobileUser = error("Unused")
            override suspend fun register(request: MobileRegisterRequest): MobileSession = error("Unused")
            override suspend fun registerPhotographer(request: MobilePhotographerRegisterRequest): MobileSession = error("Unused")
            override suspend fun logout(accessToken: String) = Unit
        }
        viewModel = LensClickViewModel(
            LensClickRepository(dao),
            mobileAuthRepository = MobileAuthRepository(api, sessionStore)
        )
        advanceUntilIdle()

        viewModel.login("a@example.com", "Secret123")
        advanceUntilIdle()
        assertEquals("a@example.com", viewModel.currentUser.value?.email)

        viewModel.logout()
        advanceUntilIdle()
        assertNull(viewModel.currentUser.value)
        assertEquals(AuthenticationState.LogoutCompleted, viewModel.authentication.value)

        viewModel.login("b@example.com", "Secret123")
        advanceUntilIdle()
        assertEquals("b@example.com", viewModel.currentUser.value?.email)
        assertEquals(2L, viewModel.currentUser.value?.id)
        assertTrue(viewModel.authentication.value is AuthenticationState.Authenticated)
    }

    private fun authRepository(store: InMemorySessionStore, status: Int = 200): MobileAuthRepository =
        MobileAuthRepository(object : MobileAuthApi {
            override suspend fun login(request: MobileAuthRequest): MobileSession {
                if (status != 200) throw MobileAuthException(
                    code = MobileAuthErrorCode.UNAUTHENTICATED,
                    statusCode = status
                )
                return MobileSession(
                    user = MobileUser(42, "Pessoa", "user@example.com", null, "client", true, false),
                    accessToken = "access-token",
                    expiresAt = "2099-01-01T00:00:00Z"
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
