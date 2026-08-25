package br.com.lensclick.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.ui.components.LcButton
import br.com.lensclick.app.ui.components.LcTextField
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.TextDim

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val user by SessionManager.user.collectAsState()

    LaunchedEffect(user) {
        if (user != null) onRegistered()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Criar conta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Text(
            text = "Comece a encontrar ou oferecer serviços fotográficos",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        LcTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = "Nome completo",
            isError = state.fieldError?.contains("nome", ignoreCase = true) == true
        )
        Spacer(Modifier.height(12.dp))

        LcTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "E-mail",
            keyboardType = KeyboardType.Email,
            isError = state.fieldError?.contains("e-mail", ignoreCase = true) == true
        )
        Spacer(Modifier.height(12.dp))

        LcTextField(
            value = state.phone,
            onValueChange = viewModel::onPhoneChange,
            label = "Telefone (com DDD)",
            keyboardType = KeyboardType.Phone,
            isError = state.fieldError?.contains("telefone", ignoreCase = true) == true
        )
        Spacer(Modifier.height(12.dp))

        LcTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Senha",
            isPassword = true,
            supportingText = "Mínimo de 8 caracteres, com letras e números",
            isError = state.fieldError?.contains("enha", ignoreCase = true) == true &&
                !state.fieldError.contains("telefone", ignoreCase = true)
        )
        Spacer(Modifier.height(20.dp))

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        state.fieldError?.let { fieldError ->
            Text(
                text = fieldError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        LcButton(text = "Criar minha conta", onClick = viewModel::submit, loading = state.loading)

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            Text("Já tem conta? ", style = MaterialTheme.typography.bodyMedium, color = TextDim)
            Text(
                text = "Entrar",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryLight,
                modifier = Modifier.clickable { onBackToLogin() }
            )
        }
    }
}
