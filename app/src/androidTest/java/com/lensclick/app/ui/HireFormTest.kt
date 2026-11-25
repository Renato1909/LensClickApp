package com.lensclick.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lensclick.app.data.local.LensClickDatabase
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.domain.Engagement
import com.lensclick.app.domain.EngagementAction
import com.lensclick.app.domain.EngagementDetail
import com.lensclick.app.domain.EngagementDraft
import com.lensclick.app.domain.EngagementGateway
import com.lensclick.app.domain.PaymentMethod
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.domain.PublicPhotographerContacts
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.ui.theme.LensClickAppTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HireFormTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var database: LensClickDatabase

    @After fun closeDatabase() {
        if (::database.isInitialized) database.close()
    }

    @Test fun validDraftCanBeReviewedEditedAndConfirmed() {
        database = Room.inMemoryDatabaseBuilder(rule.activity, LensClickDatabase::class.java).build()
        val photographer = PublicPhotographer(
            "foto-teste", "Fotógrafa de teste", "Campinas", "SP", listOf("wedding"), 175_000, null, 0
        )
        val profiles = object : PublicPhotographersGateway {
            override suspend fun list(city: String, specialty: String?, page: Int) =
                PublicPhotographerPage(listOf(photographer), 1, 1, 1)
            override suspend fun listTopRated(page: Int) = PublicPhotographerPage(emptyList(), 1, 0, 0)
            override suspend fun get(slug: String) = PublicPhotographerProfile(
                photographer, "", null, "available", "", "", "",
                PublicPhotographerContacts("", ""), emptyList(), true
            )
        }
        var created: Engagement? = null
        val gateway = object : EngagementGateway {
            override suspend fun create(
                slug: String, reference: String, priceCents: Long, proposalId: Long?, draft: EngagementDraft
            ) = Engagement(42, slug, "Cliente", photographer.name, draft, priceCents, 0, "gateway_required",
                "pending", false, "2026-01-01").also { created = it }
            override suspend fun list(): List<Engagement> = emptyList()
            override suspend fun detail(id: Long): EngagementDetail = error("Unexpected detail call")
            override suspend fun update(id: Long, action: EngagementAction): Engagement = error("Unexpected update")
            override suspend fun review(id: Long, rating: Int, comment: String): Unit = error("Unexpected review")
        }
        val model = LensClickViewModel(
            LensClickRepository(database.lensClickDao()), publicPhotographers = profiles, engagementGateway = gateway
        )
        var confirmed: Long? = null
        rule.setContent { LensClickAppTheme { HireScreen(model, photographer.slug, null, {}, { confirmed = it }) } }
        rule.onNodeWithText("Continuar para pagamento").performScrollTo().assertIsNotEnabled()

        fillEvent()
        rule.onNodeWithText("Continuar para pagamento").performScrollTo().assertIsEnabled().performClick()
        rule.onNodeWithText("Pagamento").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Total da contratação").assertIsDisplayed()
        rule.onNodeWithText(moneyInReais(175_000)).assertIsDisplayed()
        rule.onNodeWithText("Dinheiro").assertIsSelected()
        rule.onNodeWithText("Cartão de crédito").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Cartão de débito").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("PIX").performScrollTo().performClick().assertIsSelected()
        rule.onNodeWithText("Revisar contratação").performScrollTo().performClick()
        rule.onNodeWithText("Campinas – SP").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Editar informações").performScrollTo().performClick()
        fill("Observações e solicitações especiais", "Fotografar a entrada.")
        closeSoftKeyboard()
        rule.onNodeWithText("Continuar para pagamento").performScrollTo().assertIsEnabled().performClick()
        rule.onNodeWithText("Revisar contratação").performScrollTo().performClick()
        rule.onNodeWithText("Confirmar contratação").performScrollTo().performClick()
        rule.waitUntil(5_000) { confirmed == 42L }
        rule.runOnIdle {
            assertEquals(PaymentMethod.Pix, created?.service?.paymentMethod)
            assertEquals("Fotografar a entrada.", created?.service?.notes)
            assertEquals(175_000L, created?.priceCents)
        }
    }

    @Test fun fairBookingCanBeSimulatedWithoutAccountOrGateway() {
        val photographer = PublicPhotographer(
            "feira-livia-monteiro", "Lívia Monteiro", "São Paulo", "SP", listOf("wedding"), 245_000, null, 0
        )
        var returned = false
        rule.setContent { LensClickAppTheme { DemoHireScreen(photographer, 245_000) { returned = true } } }
        rule.onNodeWithText("Simulação sem envio de pedido ou cobrança.").performScrollTo().assertIsDisplayed()

        fillEvent()
        rule.onNodeWithText("Continuar para pagamento").performScrollTo().performClick()
        rule.onNodeWithText(moneyInReais(245_000)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("PIX").performScrollTo().performClick()
        rule.onNodeWithText("Revisar contratação").performScrollTo().performClick()
        rule.onNodeWithText("Concluir simulação").performScrollTo().performClick()
        rule.onNodeWithText("Simulação concluída").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Voltar ao portfólio").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(true, returned) }
    }

    private fun fill(label: String, value: String) {
        rule.onNodeWithContentDescription(label).performScrollTo().performTextInput(value)
    }

    private fun fillEvent() {
        fill("Tipo de evento", "Casamento")
        fill("Descrição do serviço (mín. 10 caracteres)", "Cobertura da cerimônia e retratos.")
        fill("Data (AAAA-MM-DD)", "2090-12-12")
        fill("Início (HH:MM)", "14:00")
        fill("Término (HH:MM)", "17:00")
        fill("Nome do local", "Salão de teste")
        fill("Endereço completo", "Rua de teste, 100")
        fill("Cidade", "Campinas")
        fill("Estado (UF)", "SP")
        closeSoftKeyboard()
    }
}
