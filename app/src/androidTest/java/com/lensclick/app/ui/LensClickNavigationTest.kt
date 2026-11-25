package com.lensclick.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import com.lensclick.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LensClickNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun awaitText(text: String) {
        try {
            rule.waitUntil(10_000) {
                rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (error: Throwable) {
            throw AssertionError("Tela não contém '$text':\n${rule.onRoot().printToString()}", error)
        }
    }

    private fun backFrom(screenTitle: String) {
        rule.waitForIdle()
        Espresso.pressBack()
        rule.waitForIdle()
        if (rule.onAllNodes(hasText(screenTitle)).fetchSemanticsNodes().isNotEmpty()) {
            Espresso.pressBack() // A primeira ação pode apenas fechar o teclado.
        }
    }

    @Test fun backFromPublicDiscoveryReturnsToOnboarding() {
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithText("Entrar para usar sua conta").assertIsDisplayed()
        backFrom("Buscar Fotógrafos")
        awaitText("Explorar fotógrafos")
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().assertIsDisplayed()
    }

    @Test fun fairCatalogAndPortfolioOpenAsGuest() {
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithText("Perfis da feira").performClick()

        awaitText("Lívia Monteiro")
        rule.onNodeWithText("Rafael Sato").assertExists()
        rule.onNodeWithText("Joana Nascimento").assertExists()
        rule.onNodeWithText("Bruno Vidal").assertExists()
        rule.onNodeWithText("Lívia Monteiro").performClick()

        awaitText("Perfil fictício para demonstração.")
        rule.onNodeWithContentDescription(
            "Casal caminhando entre convidados em uma cerimônia ao ar livre."
        ).performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Noivos sorrindo depois da cerimônia.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Avaliações").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Avaliações fictícias de demonstração").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Marina Luz").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Solicitar orçamento").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Favoritar").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Enviar mensagem").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Contratar").performScrollTo().performClick()
        awaitText("Selecionar data do evento")
    }

    @Test fun fairPortfolioActionsKeepDemonstrationDataOnDevice() {
        rule.runOnIdle { rule.activity.getSharedPreferences("lensclick_fair_demo", 0).edit().remove("chat/feira-rafael-sato").remove("quotes").apply() }
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithText("Perfis da feira").performClick()
        rule.onNodeWithText("Rafael Sato").performScrollTo().performClick()

        if (rule.onAllNodes(hasText("Favoritado")).fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("Favoritado").performScrollTo().performClick()
        }
        rule.onNodeWithText("Favoritar").performScrollTo().performClick()
        rule.onNodeWithText("Favoritado").assertIsDisplayed()
        rule.onNodeWithContentDescription("Voltar").performScrollTo().performClick()
        awaitText("Favoritos de demonstração")
        rule.onNodeWithText("Rafael Sato").performScrollTo().performClick()

        rule.onNodeWithText("Enviar mensagem").performScrollTo().performClick()
        awaitText("Conversa fictícia. As mensagens ficam neste aparelho.")
        rule.onNodeWithContentDescription("Mensagem").performScrollTo().performTextInput("Olá, preciso de um retrato.")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithText("Enviar").performScrollTo().performClick()
        rule.onNodeWithText("Olá, preciso de um retrato.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Voltar").performScrollTo().performClick()
        rule.onNodeWithText("Enviar mensagem").performScrollTo().performClick()
        rule.onNodeWithText("Olá, preciso de um retrato.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Voltar").performScrollTo().performClick()

        rule.onNodeWithText("Solicitar orçamento").performScrollTo().performClick()
        rule.onNodeWithText("Selecionar data prevista").performScrollTo().performClick()
        chooseEventDate(2090, 6, 15)
        rule.onNodeWithText("Confirmar").performScrollTo().performClick()
        rule.onNodeWithContentDescription("Local / Cidade").performScrollTo().performTextInput("Campinas")
        rule.onNodeWithContentDescription("Descrição do evento (mín. 10 caracteres)").performScrollTo()
            .performTextInput("Retratos profissionais da equipe.")
        Espresso.closeSoftKeyboard()
        rule.onNodeWithText("Enviar solicitação").performScrollTo().performClick()
        awaitText("Orçamento de demonstração salvo")
        rule.onNodeWithText("Orçamentos de demonstração").performScrollTo().performClick()
        rule.onNodeWithText("Retratos profissionais da equipe.").performScrollTo().assertIsDisplayed()
    }

    @Test fun demoQuoteTracksBothRolesWithoutAnAccount() {
        rule.runOnIdle { rule.activity.getSharedPreferences("lensclick_demo_budgets", 0).edit().remove("2").apply() }
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithContentDescription("Orçamentos").performClick()
        rule.onNodeWithText("Orçamentos de demonstração").performClick()
        awaitText("Os dados são fictícios. As alterações ficam neste aparelho.")
        rule.onNodeWithText("Rafael Sato", substring = true).performScrollTo().performClick()
        rule.onNodeWithText("Aprovar orçamento").performScrollTo().performClick()
        rule.onNodeWithText("Fotógrafo").performScrollTo().performClick()
        rule.onNodeWithText("Marcar como concluído").performScrollTo().performClick()
        rule.onNodeWithText("Histórico").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Aprovado · Cliente", substring = true).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Concluído · Fotógrafo", substring = true).performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Voltar").performClick()
        rule.onNodeWithText("Cliente").performScrollTo().performClick()
        rule.onNodeWithText("Rafael Sato", substring = true).performScrollTo().performClick()
        rule.onNodeWithText("Concluído · Fotógrafo", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun guestBudgetsKeepsNavigationAndOffersSignIn() {
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithContentDescription("Orçamentos").performClick()

        awaitText("Orçamentos e favoritos")
        rule.onNodeWithContentDescription("Orçamentos").assertIsDisplayed().assertIsSelected()
        rule.onNodeWithContentDescription("Buscar").assertIsDisplayed().performClick()
        awaitText("Buscar Fotógrafos")

        rule.onNodeWithContentDescription("Orçamentos").performClick()
        rule.onNodeWithText("Entrar na conta").performClick()
        awaitText("Bem-vindo de volta!")
        backFrom("Bem-vindo de volta!")
        awaitText("Orçamentos e favoritos")
        rule.onNodeWithContentDescription("Orçamentos").assertIsDisplayed().assertIsSelected()
    }

    @Test fun guestProfileAndOtherTabsKeepTheNavigationCapsule() {
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")

        rule.onNodeWithContentDescription("Perfil").performClick()
        awaitText("Seu perfil")
        rule.onNodeWithContentDescription("Perfil").assertIsDisplayed().assertIsSelected()
        rule.onNodeWithText("Configurações").assertDoesNotExist()

        rule.onNodeWithText("Entrar na conta").performClick()
        awaitText("Bem-vindo de volta!")
        backFrom("Bem-vindo de volta!")
        awaitText("Seu perfil")
        rule.onNodeWithContentDescription("Perfil").assertIsDisplayed().assertIsSelected()

        rule.onNodeWithContentDescription("Mensagens").performClick()
        awaitText("Mensagens")
        rule.onNodeWithContentDescription("Mensagens").assertIsDisplayed().assertIsSelected()
        rule.onNodeWithText("Entrar na conta").assertIsDisplayed()

        rule.onNodeWithContentDescription("Início").performClick()
        awaitText("Seu momento, seu fotógrafo")
        rule.onNodeWithContentDescription("Início").assertIsDisplayed().assertIsSelected()
        rule.onNodeWithContentDescription("Buscar").performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithContentDescription("Buscar").assertIsDisplayed().assertIsSelected()
    }

    @Test fun loginStartsSignedOutAndDoesNotUseDemoCredentials() {
        rule.onNodeWithText("Entrar").performScrollTo().performClick()
        awaitText("Bem-vindo de volta!")
        rule.onNodeWithText("Bem-vindo de volta!").assertIsDisplayed()
        rule.onNodeWithContentDescription("E-mail").assertIsDisplayed()
        rule.onNodeWithContentDescription("Senha").assertIsDisplayed()
        rule.onNodeWithText("Minha conta").assertDoesNotExist()
    }
}
