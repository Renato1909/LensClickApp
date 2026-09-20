package com.example.lensclickapp.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LensClickRepositoryTest {
    private val dao = FakeLensClickDao()
    private val repository = LensClickRepository(dao)

    @Test
    fun seedingTwiceKeepsExistingDataAndOneDemoAccount() = runTest {
        repository.seedDemoData()
        repository.createBudget("Novo", "20/09/2026", "Recife", "2 horas", "Ensaio", "")
        repository.seedDemoData()

        assertEquals(LensClickData.photographers.size, dao.photographerRows.value.size)
        assertEquals(LensClickData.budgets.size + 1, dao.budgetRows.value.size)
        assertEquals(1, dao.users.size)
        assertNotNull(repository.authenticate(" SEU@EMAIL.COM ", "lensclick"))
    }

    @Test
    fun registrationNormalizesProfileAndAcceptsOnlyMatchingDemoPassword() = runTest {
        val user = repository.register("  Ana Silva  ", " ANA@EXAMPLE.TEST ", "fictional-demo")!!

        assertTrue(user.id > 0)
        assertEquals("Ana Silva", user.name)
        assertEquals("ana@example.test", user.email)
        assertEquals("client", user.role)
        assertEquals(user, repository.authenticate(" Ana@Example.Test ", "fictional-demo"))
        assertNull(repository.authenticate(user.email, "wrong-demo"))
        assertNull(repository.authenticate("missing@example.test", "fictional-demo"))
    }

    @Test
    fun duplicateRegistrationDoesNotReplaceAnExistingDemoPassword() = runTest {
        val original = repository.register("Ana", "ana@example.test", "first-demo")
        val duplicate = repository.register("Outra", " ANA@EXAMPLE.TEST ", "second-demo")

        assertNull(duplicate)
        assertEquals(listOf(original), dao.users)
        assertEquals(original, repository.authenticate("ana@example.test", "first-demo"))
        assertNull(repository.authenticate("ana@example.test", "second-demo"))
    }

    @Test
    fun registeredCredentialsDoNotSurviveRepositoryRecreation() = runTest {
        val user = repository.register("Ana", "ana@example.test", "temporary-demo")!!
        val recreated = LensClickRepository(dao)

        assertEquals(user, dao.findUser(user.email))
        assertNull(recreated.authenticate(user.email, "temporary-demo"))
        recreated.seedDemoData()
        assertNotNull(recreated.authenticate("seu@email.com", "lensclick"))
    }

    @Test
    fun photographerRegistrationLinksProfileAndTrimsFields() = runTest {
        val user = repository.registerPhotographer(
            "  Júlia   Melo Souza ", " JULIA@EXAMPLE.TEST ", "fictional-demo",
            " Eventos ", " Recife, PE ", " R$ 800 ", " Retratos naturais "
        )!!

        val photographer = dao.photographerRows.value.single()
        assertEquals("photographer", user.role)
        assertEquals(user.id, photographer.userId)
        assertEquals("JM", photographer.initials)
        assertEquals("Júlia   Melo Souza", photographer.name)
        assertEquals("Eventos", photographer.specialty)
        assertEquals("Recife, PE", photographer.city)
        assertEquals("R$ 800", photographer.price)
        assertEquals("Retratos naturais", photographer.bio)
        assertEquals(user, repository.authenticate(user.email, "fictional-demo"))
    }

    @Test
    fun duplicatePhotographerRegistrationDoesNotAddAnOrphanProfile() = runTest {
        repository.register("Ana", "ana@example.test", "fictional-demo")

        val result = repository.registerPhotographer(
            "Ana", " ANA@EXAMPLE.TEST ", "other-demo", "Eventos", "Recife", "R$ 800", "Bio"
        )

        assertNull(result)
        assertTrue(dao.photographerRows.value.isEmpty())
        assertEquals("client", dao.users.single().role)
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
