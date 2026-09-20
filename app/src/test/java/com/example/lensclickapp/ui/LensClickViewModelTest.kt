package com.example.lensclickapp.ui

import androidx.lifecycle.ViewModelStore
import com.example.lensclickapp.data.FakeLensClickDao
import com.example.lensclickapp.data.LensClickRepository
import com.example.lensclickapp.data.User
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
        var callbackUser: User? = null
        viewModel.login("seu@email.com", "lensclick") { callbackUser = it }
        advanceUntilIdle()

        assertNotNull(callbackUser)
        assertEquals(callbackUser, viewModel.currentUser.value)
        viewModel.logout()
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun failedLoginCannotLeaveAPreviousUserAuthenticated() = runTest(dispatcher) {
        advanceUntilIdle()
        viewModel.login("seu@email.com", "lensclick") {}
        advanceUntilIdle()
        var callbackInvoked = false
        viewModel.login("seu@email.com", "incorrect-demo") {
            callbackInvoked = true
            assertNull(it)
        }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun registrationReportsSuccessAndDuplicateFailure() = runTest(dispatcher) {
        advanceUntilIdle()
        var success: Boolean? = null
        viewModel.register("Ana", "ana@example.test", "fictional-demo") { success = it }
        advanceUntilIdle()
        assertEquals(true, success)
        assertEquals("ana@example.test", viewModel.currentUser.value?.email)

        viewModel.register("Outra", "ANA@EXAMPLE.TEST", "other-demo") { success = it }
        advanceUntilIdle()
        assertEquals(false, success)
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun photographerRegistrationSelectsProfessionalAccount() = runTest(dispatcher) {
        advanceUntilIdle()
        var success = false
        viewModel.registerPhotographer(
            "Ana", "ana@example.test", "fictional-demo", "Eventos", "Recife", "R$ 800", "Bio"
        ) { success = it }
        advanceUntilIdle()

        assertTrue(success)
        val user = viewModel.currentUser.value!!
        assertEquals("photographer", user.role)
        assertEquals(user.id, dao.photographerRows.value.last().userId)
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
}
