package com.example.lensclickapp.ui

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import com.example.lensclickapp.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LensClickNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun backFromQuoteReturnsThroughProfileToDiscovery() {
        rule.onNodeWithText("Entrar").performClick()
        rule.onNodeWithText("Bem-vindo de volta!").assertIsDisplayed()
        rule.onNodeWithText("Entrar").performClick()
        rule.waitUntil(10_000) {
            rule.onAllNodesWithText("Buscar").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Buscar").performClick()
        rule.waitUntil(10_000) {
            rule.onAllNodesWithText("Lucas Almeida").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Lucas Almeida").performClick()
        rule.onNodeWithText("Solicitar orçamento").performClick()
        Espresso.pressBack()
        rule.onNodeWithText("Lucas Almeida").assertIsDisplayed()
        Espresso.pressBack()
        rule.onNodeWithText("Descobrir").assertIsDisplayed()
        rule.onNodeWithText("Solicitar orçamento").assertDoesNotExist()
    }

    @Test fun logoutClearsPrivateNavigation() {
        rule.onNodeWithText("Entrar").performClick()
        rule.onNodeWithText("Entrar").performClick()
        rule.waitUntil(10_000) {
            rule.onAllNodesWithText("Perfil").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Perfil").performClick()
        rule.onNodeWithText("Sair da conta").performClick()
        rule.onNodeWithText("Bem-vindo de volta!").assertIsDisplayed()
        rule.onNodeWithText("Minha conta").assertDoesNotExist()
    }
}
