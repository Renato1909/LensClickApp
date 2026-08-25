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
fun LoginScreen(
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

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
            text = "LensClick",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Text(
            text = "Encontre o fotógrafo ideal para o seu momento",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        LcTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "E-mail",
            keyboardType = KeyboardType.Email,
            isError = state.fieldError?.contains("e-mail", ignoreCase = true) == true,
            supportingText = state.fieldError?.takeIf { it.contains("e-mail", ignoreCase = true) }
        )
        Spacer(Modifier.height(12.dp))

        LcTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Senha",
            isPassword = true,
            isError = state.fieldError?.contains("enha", ignoreCase = false) == true &&
                !state.fieldError.contains("e-mail", ignoreCase = true),
            supportingText = state.fieldError?.takeIf {
                !it.contains("e-mail", ignoreCase = true) && !it.contains("conex", ignoreCase = true)
            }
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

        LcButton(
            text = "Entrar",
            onClick = viewModel::submit,
            loading = state.loading
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Não tem conta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDim
            )
            Text(
                text = "Cadastre-se",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryLight,
                modifier = Modifier.clickable { onGoToRegister() }
            )
        }
    }
}
