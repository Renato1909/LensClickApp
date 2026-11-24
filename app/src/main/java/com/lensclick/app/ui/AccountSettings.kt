package com.lensclick.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lensclick.app.BuildConfig
import com.lensclick.app.data.remote.ApiEnvironment
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.ReviewRequest
import com.lensclick.app.ui.theme.LensInk as Ink
import com.lensclick.app.ui.theme.LensLine as Line
import com.lensclick.app.ui.theme.LensMuted as Muted
import com.lensclick.app.ui.theme.LensSurface as SurfaceHigh

internal fun Context.openLensClickPage(path: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ApiEnvironment.baseUrl.trimEnd('/') + path)))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}

@Composable
internal fun SettingsScreen(
    viewModel: LensClickViewModel,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onProfessionalActivated: () -> Unit
) {
    var passwordOpen by rememberSaveable { mutableStateOf(false) }
    var passwordChanged by rememberSaveable { mutableStateOf(false) }
    var activationOpen by rememberSaveable { mutableStateOf(false) }
    var languageOpen by rememberSaveable { mutableStateOf(false) }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    SettingsPage(localized("Configurações"), onBack) {
        SettingsRow(localized("Idioma"), AppLanguage.current.label) { languageOpen = true }
        SettingsRow(localized("Alterar senha"), onClick = {
            passwordChanged = false
            passwordOpen = true
        })
        SettingsRow(localized("Meus dados")) { onNavigate(Screen.AccountData) }
        SettingsRow(localized("Segurança (2FA)")) { onNavigate(Screen.AccountSecurity) }
        SettingsRow(localized("Notificações")) { onNavigate(Screen.Notifications) }
        if (user?.role == "photographer") {
            SettingsRow(localized("Contatos para avaliação")) { onNavigate(Screen.ReviewRequests) }
        } else if (user?.role == "client") {
            SettingsRow(localized("Ativar conta de fotógrafo")) { activationOpen = true }
        }
        SettingsRow(localized("Ajuda e suporte")) { onNavigate(Screen.Support) }
        SettingsRow(localized("Sobre o Lens Click")) { onNavigate(Screen.About) }
        if (passwordChanged) AuthStatusBanner(localized("Senha alterada. As outras sessões foram encerradas."), loading = false)
    }
    if (languageOpen) {
        AlertDialog(
            onDismissRequest = { languageOpen = false },
            title = { Text(localized("Idioma do aplicativo")) },
            text = {
                Column(Modifier.selectableGroup()) {
                    Language.entries.forEach { language ->
                        val selected = AppLanguage.current == language
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(selected, role = Role.RadioButton) {
                                AppLanguage.select(language)
                                languageOpen = false
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Text(language.label, Modifier.padding(start = 12.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { languageOpen = false }) { Text(localized("Cancelar")) } }
        )
    }
    if (passwordOpen) ChangePasswordDialog(viewModel, onDismiss = { passwordOpen = false }) {
        passwordOpen = false
        passwordChanged = true
    }
    if (activationOpen) {
        ProfessionalAccountDialog(viewModel, onDismiss = { activationOpen = false }) {
            activationOpen = false
            onProfessionalActivated()
        }
    }
}

@Composable
private fun ProfessionalAccountDialog(viewModel: LensClickViewModel, onDismiss: () -> Unit, onActivated: () -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(localized("Ativar conta de fotógrafo"), color = Ink) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized("A troca libera o painel profissional. Pedidos de orçamento, favoritos e avaliações são ações de contas de cliente."), color = Muted)
                LensField(localized("Telefone da conta"), phone, { phone = it }, KeyboardType.Phone)
                if (busy) AuthStatusBanner(localized("Ativando conta profissional…"), loading = true)
                error?.let { AuthStatusBanner(it, loading = false, error = true) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                busy = true
                error = null
                viewModel.enableProfessionalAccount(phone) { result ->
                    busy = false
                    result.onSuccess { onActivated() }.onFailure {
                        val code = (it as? MobileAuthException)?.code
                        error = code?.accountActionMessage(localized("Não foi possível ativar a conta de fotógrafo."))
                            ?: localized("Não foi possível ativar a conta de fotógrafo.")
                    }
                }
            }, enabled = !busy && phone.trim().length in 8..30) { Text(localized("Ativar conta")) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(localized("Cancelar")) } },
        containerColor = SurfaceHigh
    )
}

@Composable
private fun ChangePasswordDialog(viewModel: LensClickViewModel, onDismiss: () -> Unit, onSaved: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val valid = password.length >= 8 && password.any(Char::isLetter) && password.any { it in '0'..'9' }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(localized("Alterar senha"), color = Ink) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(localized("Use pelo menos 8 caracteres, incluindo uma letra e um número."), color = Muted)
                LensField(localized("Senha atual"), current, { current = it }, password = true)
                LensField(localized("Nova senha"), password, { password = it }, password = true)
                LensField(localized("Confirmar nova senha"), confirmation, { confirmation = it }, password = true)
                if (confirmation.isNotEmpty() && confirmation != password) {
                    Text(localized("As senhas precisam coincidir."), color = MaterialTheme.colorScheme.error)
                }
                if (busy) AuthStatusBanner(localized("Alterando senha…"), loading = true)
                error?.let { AuthStatusBanner(it, loading = false, error = true) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    busy = true
                    error = null
                    viewModel.changePassword(current, password) { result ->
                        busy = false
                        when (result) {
                            null -> onSaved()
                            MobileAuthErrorCode.FORBIDDEN -> error = localized("A senha atual está incorreta.")
                            MobileAuthErrorCode.CONFLICT -> error = localized("A nova senha precisa ser diferente da senha atual.")
                            else -> error = result.accountActionMessage(localized("Não foi possível alterar a senha."))
                        }
                    }
                },
                enabled = !busy && current.isNotBlank() && valid && password == confirmation
            ) { Text(localized("Salvar nova senha")) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(localized("Cancelar")) } },
        containerColor = SurfaceHigh
    )
}

@Composable
internal fun SupportScreen(onBack: () -> Unit) {
    SettingsPage(localized("Ajuda e suporte"), onBack) {
        WebSettingsLinks(
            localized("Perguntas frequentes") to "/#faq",
            localized("Falar com a equipe") to "/#contato"
        )
    }
}

@Composable
internal fun AboutScreen(onBack: () -> Unit) {
    SettingsPage(localized("Sobre o Lens Click"), onBack) {
        BrandMark()
        Text(localized("Versão {0}", BuildConfig.VERSION_NAME), color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        WebSettingsLinks(
            localized("Site do LensClick") to "/",
            localized("Termos de uso") to "/termos",
            localized("Política de privacidade") to "/privacidade"
        )
    }
}

@Composable
internal fun WebSettingsLinks(vararg links: Pair<String, String>) {
    val context = LocalContext.current
    var failed by remember { mutableStateOf(false) }
    links.forEach { (label, path) ->
        SettingsRow(label) { failed = !context.openLensClickPage(path) }
    }
    if (failed) AuthStatusBanner(localized("Não foi possível abrir o navegador."), loading = false, error = true)
}

@Composable
internal fun NotificationsScreen(viewModel: LensClickViewModel, onBack: () -> Unit, onConversation: (Long) -> Unit) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val loading by viewModel.conversationsLoading.collectAsStateWithLifecycle()
    val failed by viewModel.conversationsFailed.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadConversations() }

    SettingsPage(localized("Notificações"), onBack) {
        Text(localized("Mensagens não lidas"), color = Muted, style = MaterialTheme.typography.titleMedium)
        when {
            loading -> AuthStatusBanner(localized("Buscando novas mensagens…"), loading = true)
            failed -> {
                AuthStatusBanner(localized("Não foi possível carregar as notificações."), loading = false, error = true)
                TextButton(onClick = viewModel::loadConversations) { Text(localized("Tentar novamente")) }
            }
            else -> {
                val unread = conversations.filter { it.unreadCount > 0 }
                if (unread.isEmpty()) Text(localized("Você está em dia com suas mensagens."), color = Muted)
                unread.forEach { conversation ->
                    val count = conversation.unreadCount
                    SettingsRow(
                        title = conversation.counterpartName,
                        detail = if (count == 1) localized("1 mensagem não lida") else localized("{0} mensagens não lidas", count)
                    ) { onConversation(conversation.id) }
                }
                TextButton(onClick = viewModel::loadConversations) { Text(localized("Atualizar")) }
            }
        }
    }
}

@Composable
internal fun ReviewRequestsScreen(viewModel: LensClickViewModel, onBack: () -> Unit) {
    var requests by remember { mutableStateOf<List<ReviewRequest>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var updating by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    fun load() {
        loading = true
        error = null
        viewModel.loadReviewRequests { result ->
            loading = false
            result.onSuccess { requests = it }.onFailure {
                error = (it as? MobileAuthException)?.code?.accountActionMessage(localized("Não foi possível carregar os contatos."))
                    ?: localized("Não foi possível carregar os contatos.")
            }
        }
    }
    LaunchedEffect(Unit) { load() }

    SettingsPage(localized("Contatos para avaliação"), onBack) {
        if (loading) AuthStatusBanner(localized("Carregando contatos…"), loading = true)
        error?.let {
            AuthStatusBanner(it, loading = false, error = true)
            TextButton(onClick = { load() }, enabled = !loading && updating == null) { Text(localized("Atualizar contatos")) }
        }
        val pending = requests?.filter { it.status == "pending" }
        if (pending?.isEmpty() == true) Text(localized("Nenhum contato aguardando confirmação."), color = Muted)
        pending?.forEach { request ->
            val shape = MaterialTheme.shapes.medium
            Column(
                Modifier.fillMaxWidth().clip(shape).background(SurfaceHigh).border(1.dp, Line, shape).padding(14.dp)
            ) {
                Text(request.counterpartName, color = Ink, style = MaterialTheme.typography.titleMedium)
                Text(request.message, color = Muted, style = MaterialTheme.typography.bodyMedium)
                fun resolve(confirm: Boolean) {
                    updating = request.id
                    error = null
                    viewModel.resolveReviewRequest(request.id, confirm) { result ->
                        updating = null
                        result.onSuccess { requests = requests?.filterNot { it.id == request.id } }
                            .onFailure { error = localized("Não foi possível atualizar a solicitação. Atualize a lista e tente novamente.") }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { resolve(true) }, enabled = updating == null && !loading) { Text(localized("Confirmar")) }
                    TextButton(onClick = { resolve(false) }, enabled = updating == null && !loading) { Text(localized("Recusar")) }
                }
            }
        }
    }
}

@Composable
internal fun SettingsPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            PageTitle(title, Modifier.weight(1f))
        }
        content()
    }
}

@Composable
private fun SettingsRow(title: String, detail: String? = null, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shape).background(SurfaceHigh)
            .border(1.dp, Line, shape).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, style = MaterialTheme.typography.bodyLarge)
            detail?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodySmall) }
        }
        Text("›", color = Muted, fontSize = 20.sp)
    }
}

internal fun MobileAuthErrorCode.accountActionMessage(fallback: String): String = when (this) {
    MobileAuthErrorCode.UNAUTHENTICATED -> localized("Sua sessão expirou. Entre novamente.")
    MobileAuthErrorCode.NETWORK -> localized("Não foi possível conectar. Verifique sua internet.")
    MobileAuthErrorCode.RATE_LIMITED -> localized("Muitas tentativas. Aguarde antes de tentar novamente.")
    else -> fallback
}
