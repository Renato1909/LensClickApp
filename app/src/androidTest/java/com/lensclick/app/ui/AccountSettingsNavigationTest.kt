package com.lensclick.app.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lensclick.app.data.local.LensClickDatabase
import com.lensclick.app.data.local.LensClickRepository
import com.lensclick.app.data.remote.ApiEnvironment
import com.lensclick.app.data.remote.HttpAccountGateway
import com.lensclick.app.data.remote.HttpConversationsGateway
import com.lensclick.app.data.remote.HttpTwoFactorGateway
import com.lensclick.app.data.remote.auth.HttpMobileAuthApi
import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthRepository
import com.lensclick.app.ui.theme.LensClickAppTheme
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AccountSettingsNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: LensClickDatabase
    private val requests = mutableListOf<HttpRequest>()
    private var unread = true

    private fun openApp(signedIn: Boolean = true) {
        database = Room.inMemoryDatabaseBuilder(rule.activity, LensClickDatabase::class.java).build()
        val session = InMemorySessionStore()
        if (signedIn) session.writeAccessToken("test-session")
        val transport = object : HttpTransport {
            override suspend fun execute(request: HttpRequest): HttpResponse {
                requests.add(request)
                return when (request.method to request.path) {
                    "POST" to "/api/mobile/auth/login" -> HttpResponse(401)
                    "GET" to "/api/mobile/auth/me" -> HttpResponse(200, body =
                        """{"user":{"id":21,"name":"Pessoa de teste","email":"teste@example.com","role":"client"}}""")
                    "GET" to "/api/account/2fa/status" -> HttpResponse(200, body = """{"enabled":false}""")
                    "GET" to "/api/account/export" -> HttpResponse(200, body = """{"user":{"id":21}}""")
                    "PUT" to "/api/account/password" -> HttpResponse(200, body = """{"message":"updated"}""")
                    "GET" to "/api/account/conversations" -> HttpResponse(200, body = """
                        {"conversations":[
                          {"id":10,"counterpartName":"Primeira conversa","unreadCount":0},
                          {"id":20,"counterpartName":"Segunda conversa","unreadCount":${if (unread) 2 else 0}}
                        ]}
                    """.trimIndent())
                    "GET" to "/api/account/conversations/20/messages" -> HttpResponse(200, body =
                        """{"messages":[{"id":51,"text":"Mensagem da segunda conversa"}]}""")
                    "PUT" to "/api/account/conversations/20/read" -> {
                        unread = false
                        HttpResponse(200)
                    }
                    "POST" to "/api/account/conversations/20/messages" -> HttpResponse(201, body =
                        """{"message":{"id":52,"text":"Resposta de teste","isMine":true}}""")
                    else -> error("Rota de teste inesperada ${request.method} ${request.path}")
                }
            }
        }
        val factory = viewModelFactory {
            initializer {
                LensClickViewModel(
                    LensClickRepository(database.lensClickDao()),
                    mobileAuthRepository = MobileAuthRepository(HttpMobileAuthApi(transport), session),
                    accountGateway = HttpAccountGateway(transport, session),
                    conversationsGateway = HttpConversationsGateway(transport, session),
                    twoFactorGateway = HttpTwoFactorGateway(transport, session)
                )
            }
        }
        rule.setContent {
            LensClickAppTheme { LensClickApp(viewModel(factory = factory)) }
        }
    }

    @After fun closeDatabase() {
        if (::database.isInitialized) database.close()
    }

    private fun awaitText(text: String) {
        rule.waitUntil(10_000) { rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun openAccount() {
        openApp()
        awaitText("Seu momento, seu fotógrafo")
        rule.onNodeWithContentDescription("Perfil").performClick()
        awaitText("Pessoa de teste")
        rule.onNodeWithContentDescription("Perfil").assertIsDisplayed().assertIsSelected()
    }

    @Test fun failedLoginReturnsToGuestProfileWithNavigation() {
        openApp(signedIn = false)
        rule.onNodeWithText("Explorar fotógrafos").performScrollTo().performClick()
        awaitText("Buscar Fotógrafos")
        rule.onNodeWithContentDescription("Perfil").performClick()
        awaitText("Seu perfil")
        rule.onNodeWithText("Entrar na conta").performClick()
        rule.onNodeWithContentDescription("E-mail").performTextInput("teste@example.com")
        rule.onNodeWithContentDescription("Senha").performTextInput("SenhaTeste1")
        rule.onNodeWithText("Entrar").performScrollTo().performClick()
        awaitText("E-mail ou senha inválidos.")
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        awaitText("Seu perfil")
        rule.onNodeWithContentDescription("Perfil").assertIsDisplayed().assertIsSelected()
    }

    @Test fun settingsEntryPointsAndAccountSectionsNavigate() {
        openAccount()
        rule.onNodeWithContentDescription("Configurações").performClick()
        awaitText("Alterar senha")
        rule.onNodeWithText("Ajuda e suporte").performScrollTo().performClick()
        awaitText("Perguntas frequentes")
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Alterar senha")
        rule.onNodeWithText("Sobre o Lens Click").performScrollTo().performClick()
        awaitText("Política de privacidade")
        rule.onNodeWithText("Termos de uso").assertIsDisplayed()
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Alterar senha")
        rule.onNodeWithText("Segurança (2FA)").performScrollTo().performClick()
        awaitText("Configurar 2FA (TOTP)")
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Alterar senha")
        rule.onNodeWithText("Meus dados").performScrollTo().performClick()
        awaitText("Exportar meus dados")
        rule.onNodeWithText("Excluir minha conta").performClick()
        awaitText("Excluir Conta Permanentemente")
        rule.onNodeWithText("Cancelar").performClick()
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Alterar senha")
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Pessoa de teste")
        rule.onNodeWithText("Configurações").performScrollTo().performClick()
        awaitText("Alterar senha")
    }

    @Test fun appLanguageUpdatesNavigationAndPersists() {
        openAccount()
        rule.onNodeWithContentDescription("Configurações").performClick()
        try {
            rule.onNodeWithText("Idioma").performClick()
            rule.onNodeWithText("English").performClick()
            awaitText("Change password")
            rule.onNodeWithText("Language").performClick()
            rule.onNodeWithText("Español").performClick()
            awaitText("Cambiar contraseña")
            rule.runOnIdle {
                val preferences = rule.activity.getSharedPreferences("lensclick_preferences", Activity.MODE_PRIVATE)
                assertEquals("es", preferences.getString("language", null))
                AppLanguage.initialize(rule.activity.applicationContext)
                assertEquals(Language.Spanish, AppLanguage.current)
            }
            rule.onNodeWithContentDescription("Atrás").performClick()
            rule.onNodeWithContentDescription("Buscar").performClick()
            awaitText("Buscar Fotógrafos")
            rule.onNodeWithContentDescription("Presupuestos").performClick()
            awaitText("Mis solicitudes y favoritos")
        } finally {
            rule.runOnIdle { AppLanguage.select(Language.Portuguese) }
        }
    }

    @Test fun settingsChangesPasswordThroughAccountEndpoint() {
        openAccount()
        rule.onNodeWithContentDescription("Configurações").performClick()
        rule.onNodeWithText("Alterar senha").performClick()
        rule.onNodeWithContentDescription("Senha atual").performTextInput("SenhaTeste1")
        rule.onNodeWithContentDescription("Nova senha").performTextInput("SenhaTeste2")
        rule.onNodeWithContentDescription("Confirmar nova senha").performTextInput("SenhaTeste2")
        rule.onNodeWithText("Salvar nova senha").performClick()
        awaitText("Senha alterada. As outras sessões foram encerradas.")
        rule.runOnIdle {
            val request = requests.single { it.path == "/api/account/password" }
            assertEquals("PUT", request.method)
            assertEquals("Bearer test-session", request.headers["Authorization"])
            val body = JSONObject(request.body!!)
            assertEquals("SenhaTeste1", body.getString("currentPassword"))
            assertEquals("SenhaTeste2", body.getString("newPassword"))
        }
    }

    @Test fun exportWritesJsonToSelectedDocument() {
        openAccount()
        rule.onNodeWithText("Meus dados").performScrollTo().performClick()
        awaitText("Exportar meus dados")
        val output = File.createTempFile("lensclick-export-", ".json", rule.activity.cacheDir)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val filter = IntentFilter(Intent.ACTION_CREATE_DOCUMENT).apply {
            addDataType("application/json")
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(Uri.fromFile(output)))
        val monitor = instrumentation.addMonitor(filter, result, true)
        try {
            rule.onNodeWithText("Exportar meus dados").performScrollTo().performClick()
            awaitText("Cópia dos seus dados salva em JSON.")
            assertEquals(1, monitor.hits)
            assertEquals(21, JSONObject(output.readText()).getJSONObject("user").getInt("id"))
        } finally {
            instrumentation.removeMonitor(monitor)
            output.delete()
        }
    }

    @Test fun notificationOpensCorrectChatAndSendsToThatConversation() {
        openAccount()
        rule.onNodeWithText("Notificações").performScrollTo().performClick()
        awaitText("2 mensagens não lidas")
        rule.onNodeWithText("Segunda conversa").performClick()
        awaitText("Mensagem da segunda conversa")
        rule.onNodeWithText("Primeira conversa").assertDoesNotExist()
        rule.onNodeWithText("Digite sua mensagem...").performTextInput("Resposta de teste")
        rule.onNodeWithContentDescription("Enviar mensagem").performClick()
        awaitText("Resposta de teste")
        rule.onNodeWithContentDescription("Voltar").performClick()
        awaitText("Você está em dia com suas mensagens.")
        rule.runOnIdle {
            val request = requests.single { it.method == "POST" }
            assertEquals("/api/account/conversations/20/messages", request.path)
            assertEquals("Resposta de teste", JSONObject(request.body!!).getString("message"))
        }
    }

    @Test fun forgotPasswordOpensOfficialRecoveryForm() {
        openApp(signedIn = false)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val baseUrl = Uri.parse(ApiEnvironment.baseUrl)
        val filter = IntentFilter(Intent.ACTION_VIEW).apply {
            addDataScheme(baseUrl.scheme)
            addDataAuthority(baseUrl.host, null)
            addDataPath("/recuperar-senha", android.os.PatternMatcher.PATTERN_LITERAL)
        }
        val monitor = instrumentation.addMonitor(filter, null, true)
        try {
            rule.onNodeWithText("Entrar").performScrollTo().performClick()
            awaitText("Esqueci minha senha")
            rule.onNodeWithText("Esqueci minha senha").performScrollTo().performClick()
            rule.waitUntil(5_000) { monitor.hits == 1 }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }
}
