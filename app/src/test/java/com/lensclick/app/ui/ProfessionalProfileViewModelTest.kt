package com.lensclick.app.ui

import com.lensclick.app.data.local.FakeLensClickDao
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.ProfessionalProfileGateway
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfessionalProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(gateway: ProfessionalProfileGateway?): LensClickViewModel =
        LensClickViewModel(LensClickRepository(FakeLensClickDao()), professionalProfileGateway = gateway)

    @Test
    fun loadExposesRemoteProfile() = runTest(dispatcher) {
        val profile = ProfessionalProfile(slug = "ana-12", bio = "Luz natural", city = "Recife", state = "PE")
        val vm = viewModel(FakeProfileGateway(loadResult = profile))

        vm.loadProfessionalProfile()
        advanceUntilIdle()

        val state = vm.professionalProfile.value
        assertTrue(state is ProfessionalProfileState.Ready)
        assertEquals("ana-12", (state as ProfessionalProfileState.Ready).profile.slug)
    }

    @Test
    fun loadFailureExposesError() = runTest(dispatcher) {
        val vm = viewModel(FakeProfileGateway(loadError = MobileAuthException(MobileAuthErrorCode.NETWORK)))

        vm.loadProfessionalProfile()
        advanceUntilIdle()

        assertEquals(ProfessionalProfileState.Error, vm.professionalProfile.value)
    }

    @Test
    fun loadWithoutGatewayExposesError() = runTest(dispatcher) {
        val vm = viewModel(null)

        vm.loadProfessionalProfile()
        advanceUntilIdle()

        assertEquals(ProfessionalProfileState.Error, vm.professionalProfile.value)
    }

    @Test
    fun saveSuccessExposesSavedProfile() = runTest(dispatcher) {
        val saved = ProfessionalProfile(slug = "ana-12", isPublished = true)
        val vm = viewModel(FakeProfileGateway(saveResult = saved))
        vm.loadProfessionalProfile()
        advanceUntilIdle()

        vm.saveProfessionalProfile(ProfessionalProfile(bio = "Luz natural", isPublished = true))
        advanceUntilIdle()

        val state = vm.professionalProfile.value as ProfessionalProfileState.Ready
        assertTrue(state.saved)
        assertTrue(state.profile.isPublished)
        assertFalse(state.saving)
    }

    @Test
    fun savePublicationErrorExposesCodeAndKeepsDraft() = runTest(dispatcher) {
        val gateway = FakeProfileGateway(saveError = MobileAuthException(MobileAuthErrorCode.INVALID_INPUT, statusCode = 400))
        val vm = viewModel(gateway)
        vm.loadProfessionalProfile()
        advanceUntilIdle()

        vm.saveProfessionalProfile(ProfessionalProfile(bio = "curta", isPublished = true))
        advanceUntilIdle()

        val state = vm.professionalProfile.value as ProfessionalProfileState.Ready
        assertEquals(MobileAuthErrorCode.INVALID_INPUT, state.error)
        assertFalse(state.saved)
        assertEquals("curta", state.profile.bio)
    }
}

private class FakeProfileGateway(
    private val loadResult: ProfessionalProfile = ProfessionalProfile(),
    private val loadError: Throwable? = null,
    private val saveResult: ProfessionalProfile = ProfessionalProfile(),
    private val saveError: Throwable? = null
) : ProfessionalProfileGateway {
    override suspend fun load(): ProfessionalProfile {
        loadError?.let { throw it }
        return loadResult
    }

    override suspend fun save(profile: ProfessionalProfile): ProfessionalProfile {
        saveError?.let { throw it }
        return saveResult
    }
}