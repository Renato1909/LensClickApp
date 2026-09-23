package com.example.lensclickapp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import com.example.lensclickapp.MainActivity
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

    @Test fun backFromQuoteReturnsThroughProfileToDiscovery() {
        rule.onNodeWithText("Entrar").performClick()
        rule.onNodeWithText("Bem-vindo de volta!").assertIsDisplayed()
        rule.onNodeWithText("Entrar").performClick()
        awaitText("Buscar")
        rule.onNodeWithText("Buscar").performClick()
        awaitText("Lucas Almeida")
        rule.onNodeWithText("Lucas Almeida").performClick()
        rule.onNodeWithText("Solicitar orçamento").performClick()
        Espresso.pressBack()
        awaitText("Lucas Almeida")
        rule.onNodeWithText("Lucas Almeida").assertIsDisplayed()
        Espresso.pressBack()
        awaitText("Descobrir")
        rule.onNodeWithText("Descobrir").assertIsDisplayed()
        rule.onNodeWithText("Solicitar orçamento").assertDoesNotExist()
    }

    @Test fun logoutClearsPrivateNavigation() {
        rule.onNodeWithText("Entrar").performClick()
        rule.onNodeWithText("Entrar").performClick()
        awaitText("Perfil")
        rule.onNodeWithText("Perfil").performClick()
        rule.onNodeWithText("Sair da conta").performClick()
        awaitText("Bem-vindo de volta!")
        rule.onNodeWithText("Bem-vindo de volta!").assertIsDisplayed()
        rule.onNodeWithText("Minha conta").assertDoesNotExist()
    }
}
