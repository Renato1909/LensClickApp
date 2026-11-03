package com.example.lensclickapp.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LensClickRepositoryTest {
    private val dao = FakeLensClickDao()
    private val repository = LensClickRepository(dao)

    @Test
    fun seedingTwiceKeepsCatalogDataWithoutCreatingAnAccount() = runTest {
        repository.seedDemoData()
        repository.createBudget("Novo", "20/09/2026", "Recife", "2 horas", "Ensaio", "")
        repository.seedDemoData()

        assertEquals(LensClickData.photographers.size, dao.photographerRows.value.size)
        assertEquals(LensClickData.budgets.size + 1, dao.budgetRows.value.size)
        assertEquals(0, dao.users.size)
    }

    @Test
    fun newBudgetPreservesRequestDetailsAndStartsWaiting() = runTest {
        repository.createBudget(
            " Casamento ", " 20/09/2026 ", " Recife, PE ", " 4 horas ",
            " Cerimônia e festa ", " Área externa "
        )

        val budget = dao.budgetRows.value.single()
        assertEquals("Casamento", budget.title)
        assertEquals("20/09/2026 • Recife, PE", budget.date)
        assertEquals("Aguardando", budget.status)
        assertEquals("4 horas", budget.info)
        assertEquals("Cerimônia e festa", budget.description)
        assertEquals("Área externa", budget.additionalInfo)
    }
}
