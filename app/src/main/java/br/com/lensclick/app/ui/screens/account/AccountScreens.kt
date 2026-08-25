package br.com.lensclick.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.ui.components.LcButton
import br.com.lensclick.app.ui.components.LcTextField
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.Surface
import br.com.lensclick.app.ui.theme.TextDim

/**
 * Hub "Minha conta": resumo do perfil e atalhos para editar dados,
 * alterar senha, excluir conta e sair.
 */
@Composable
fun AccountScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val user by SessionManager.user.collectAsState()
    val current = user ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Minha conta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(current.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(current.email, style = MaterialTheme.typography.bodyMedium, color = TextDim)
                Text(current.phone, style = MaterialTheme.typography.bodyMedium, color = TextDim)
                Text(
                    text = if (current.role == "photographer") "Fotógrafo(a)" else "Cliente",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryLight,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        AccountNavItem("Orçamentos") { navController.navigate("quotes") }
        AccountNavItem("Mensagens") { navController.navigate("conversations") }
        AccountNavItem("Editar dados pessoais") { navController.navigate("edit-profile") }
        AccountNavItem("Alterar senha") { navController.navigate("change-password") }
        if (current.role == "photographer") {
            AccountNavItem("Perfil profissional") { navController.navigate("professional-profile") }
        }
        AccountNavItem("Sair da conta", tint = TextDim) {
            onLogout()
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TextDim.copy(alpha = 0.2f))
        AccountNavItem("Excluir minha conta", tint = MaterialTheme.colorScheme.error) {
            navController.navigate("delete-account")
        }
    }
}

@Composable
private fun AccountNavItem(label: String, tint: androidx.compose.ui.graphics.Color = PrimaryLight, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = tint, style = MaterialTheme.typography.bodyLarge)
        Text(text = "›", color = TextDim, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val viewModel: EditProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Voltar", color = PrimaryLight) }
        Text(
            text = "Dados pessoais",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Spacer(Modifier.height(16.dp))

        LcTextField(value = state.name, onValueChange = viewModel::onNameChange, label = "Nome completo")
        Spacer(Modifier.height(12.dp))
        LcTextField(value = state.email, onValueChange = viewModel::onEmailChange, label = "E-mail")
        Spacer(Modifier.height(12.dp))
        LcTextField(value = state.phone, onValueChange = viewModel::onPhoneChange, label = "Telefone")

        val emailChanged = state.email.trim() != (SessionManager.user.value?.email ?: "")
        var currentPassword by remember { mutableStateOf("") }
        if (emailChanged) {
            Spacer(Modifier.height(12.dp))
            LcTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Senha atual (exigida para mudar o e-mail)",
                isPassword = true
            )
        }

        Spacer(Modifier.height(20.dp))
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
        state.fieldError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
        if (state.saved) {
            Text("Dados atualizados com sucesso.", color = br.com.lensclick.app.ui.theme.Success, modifier = Modifier.padding(bottom = 8.dp))
        }

        LcButton(
            text = "Salvar alterações",
            onClick = { viewModel.submit(if (emailChanged) currentPassword else null) },
            loading = state.loading
        )
    }
}

@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val viewModel: ChangePasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Voltar", color = PrimaryLight) }
        Text(
            text = "Alterar senha",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Spacer(Modifier.height(16.dp))

        LcTextField(value = state.currentPassword, onValueChange = viewModel::onCurrentChange, label = "Senha atual", isPassword = true)
        Spacer(Modifier.height(12.dp))
        LcTextField(
            value = state.newPassword,
            onValueChange = viewModel::onNewChange,
            label = "Nova senha",
            isPassword = true,
            supportingText = "Mínimo de 8 caracteres, com letras e números"
        )
        Spacer(Modifier.height(12.dp))
        LcTextField(value = state.confirmPassword, onValueChange = viewModel::onConfirmChange, label = "Confirmar nova senha", isPassword = true)

        Spacer(Modifier.height(20.dp))
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
        state.success?.let { Text(it, color = br.com.lensclick.app.ui.theme.Success, modifier = Modifier.padding(bottom = 8.dp)) }

        LcButton(text = "Salvar nova senha", onClick = viewModel::submit, loading = state.loading)
    }
}

@Composable
fun DeleteAccountScreen(onBack: () -> Unit, onDeleted: () -> Unit) {
    val viewModel: DeleteAccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Voltar", color = PrimaryLight) }
        Text(
            text = "Excluir conta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Esta ação é permanente. Todos os seus dados, incluindo portfólio e mensagens, serão removidos.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim
        )
        Spacer(Modifier.height(16.dp))

        LcTextField(value = state.password, onValueChange = viewModel::onPasswordChange, label = "Senha atual", isPassword = true)

        Spacer(Modifier.height(20.dp))
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }

        LcButton(text = "Excluir definitivamente", onClick = { viewModel.submit(onDone = onDeleted) }, loading = state.loading)
    }
}
