package com.lensclick.app.ui

import com.lensclick.app.data.local.FakeLensClickDao
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.PortfolioGateway
import com.lensclick.app.domain.PortfolioItem
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
class PortfolioViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val item = PortfolioItem(7, "Praia", "Retrato", 0, true, "portrait", "/api/account/portfolio/7/image")

    private fun viewModel(gateway: PortfolioGateway?): LensClickViewModel =
        LensClickViewModel(LensClickRepository(FakeLensClickDao()), portfolioGateway = gateway)

    @Test
    fun loadExposesItems() = runTest(dispatcher) {
        val vm = viewModel(FakePortfolioGateway(items = listOf(item)))
        vm.loadPortfolio()
        advanceUntilIdle()
        val state = vm.portfolio.value
        assertTrue(state is PortfolioState.Ready)
        assertEquals(1, (state as PortfolioState.Ready).items.size)
    }

    @Test
    fun loadFailureExposesError() = runTest(dispatcher) {
        val vm = viewModel(FakePortfolioGateway(listError = MobileAuthException(MobileAuthErrorCode.NETWORK)))
        vm.loadPortfolio()
        advanceUntilIdle()
        assertEquals(PortfolioState.Error, vm.portfolio.value)
    }

    @Test
    fun loadWithoutGatewayExposesError() = runTest(dispatcher) {
        val vm = viewModel(null)
        vm.loadPortfolio()
        advanceUntilIdle()
        assertEquals(PortfolioState.Error, vm.portfolio.value)
    }

    @Test
    fun addSuccessReloadsList() = runTest(dispatcher) {
        val gateway = FakePortfolioGateway(items = listOf(item))
        val vm = viewModel(gateway)
        vm.loadPortfolio()
        advanceUntilIdle()

        vm.addPortfolioImage(byteArrayOf(1), "image/jpeg", "Alt", "Cap", "portrait")
        advanceUntilIdle()

        val state = vm.portfolio.value as PortfolioState.Ready
        assertFalse(state.busy)
        assertEquals("Imagem adicionada.", state.message)
        assertTrue(gateway.added)
    }

    @Test
    fun addConflictExposesError() = runTest(dispatcher) {
        val gateway = FakePortfolioGateway(items = listOf(item), addError = MobileAuthException(MobileAuthErrorCode.CONFLICT, statusCode = 409))
        val vm = viewModel(gateway)
        vm.loadPortfolio()
        advanceUntilIdle()

        vm.addPortfolioImage(byteArrayOf(1), "image/jpeg", "Alt", "Cap", "portrait")
        advanceUntilIdle()

        val state = vm.portfolio.value as PortfolioState.Ready
        assertEquals(MobileAuthErrorCode.CONFLICT, state.error)
        assertFalse(state.busy)
    }

    @Test
    fun updateSuccessExposesItems() = runTest(dispatcher) {
        val reordered = listOf(item.copy(position = 0, isCover = false))
        val gateway = FakePortfolioGateway(items = listOf(item), updateResult = reordered)
        val vm = viewModel(gateway)
        vm.loadPortfolio()
        advanceUntilIdle()

        vm.updatePortfolio(reordered)
        advanceUntilIdle()

        val state = vm.portfolio.value as PortfolioState.Ready
        assertEquals(reordered, state.items)
        assertEquals("Portfólio atualizado.", state.message)
    }

    @Test
    fun removeSuccessReloadsList() = runTest(dispatcher) {
        val gateway = FakePortfolioGateway(items = listOf(item))
        val vm = viewModel(gateway)
        vm.loadPortfolio()
        advanceUntilIdle()

        vm.removePortfolioItem(7)
        advanceUntilIdle()

        val state = vm.portfolio.value as PortfolioState.Ready
        assertTrue(gateway.removed)
        assertEquals("Imagem removida.", state.message)
    }
}

private class FakePortfolioGateway(
    private val items: List<PortfolioItem> = emptyList(),
    private val listError: Throwable? = null,
    private val addError: Throwable? = null,
    private val updateResult: List<PortfolioItem>? = null
) : PortfolioGateway {
    var added = false
    var removed = false

    override suspend fun list(): List<PortfolioItem> {
        listError?.let { throw it }
        return items
    }

    override suspend fun add(image: ByteArray, imageContentType: String, altText: String, caption: String, category: String): PortfolioItem {
        added = true
        addError?.let { throw it }
        return items.first()
    }

    override suspend fun update(items: List<PortfolioItem>): List<PortfolioItem> = updateResult ?: items

    override suspend fun remove(id: Long) {
        removed = true
    }
}