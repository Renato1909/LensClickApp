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

    @Test fun backFromPublicDiscoveryReturnsToOnboarding() {
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithText("Entrar para usar sua conta").assertIsDisplayed()
        rule.waitForIdle()
        Espresso.pressBack()
        rule.waitForIdle()
        if (rule.onAllNodes(hasText("Buscar Fotógrafos")).fetchSemanticsNodes().isNotEmpty()) {
            Espresso.pressBack() // A primeira ação pode apenas fechar o teclado.
        }
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
        rule.onNodeWithText("Solicitar orçamento").assertDoesNotExist()
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
        Espresso.pressBack()
        awaitText("Orçamentos e favoritos")
        rule.onNodeWithContentDescription("Orçamentos").assertIsDisplayed().assertIsSelected()
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
