package com.example.lensclickapp.ui

import androidx.lifecycle.ViewModelStore
import com.example.lensclickapp.data.FakeLensClickDao
import com.example.lensclickapp.data.LensClickRepository
import com.example.lensclickapp.data.PublicPhotographerPage
import com.example.lensclickapp.data.PublicPhotographersGateway
import com.example.lensclickapp.data.User
import com.example.lensclickapp.data.auth.HttpResponse
import com.example.lensclickapp.data.auth.InMemorySessionStore
import com.example.lensclickapp.data.auth.MobileAuthApi
import com.example.lensclickapp.data.auth.MobileAuthException
import com.example.lensclickapp.data.auth.MobileAuthErrorCode
import com.example.lensclickapp.data.auth.MobileAuthRepository
import com.example.lensclickapp.data.auth.MobileAuthRequest
import com.example.lensclickapp.data.auth.MobileSession
import com.example.lensclickapp.data.auth.MobileUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        }
        viewModel = LensClickViewModel(LensClickRepository(dao), gateway)
        store.put("test", viewModel)

        viewModel.searchPublicPhotographers("Recife", "wedding", 2)
        advanceUntilIdle()

        assertEquals(Triple("Recife", "wedding", 2), requested)
        assertEquals(PublicSearchState.Ready(PublicPhotographerPage(emptyList(), 2, 0, 0)), viewModel.publicSearch.value)
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
