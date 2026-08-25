package br.com.lensclick.app.ui.screens.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.lensclick.app.ui.components.LcButton
import br.com.lensclick.app.ui.components.LcTextField
import br.com.lensclick.app.ui.theme.Primary
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.Success
import br.com.lensclick.app.ui.theme.TextDim

/**
 * Formulário para o cliente pedir um orçamento ao fotógrafo [slug].
 */
@Composable
fun RequestQuoteScreen(slug: String, onBack: () -> Unit) {
    val viewModel: RequestQuoteViewModel = viewModel(
        key = "request-quote-$slug",
        factory = viewModelFactory { initializer { RequestQuoteViewModel(slug) } }
    )
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
            text = "Solicitar orçamento",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Descreva o evento para o fotógrafo responder pelo app.",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
        Spacer(Modifier.height(16.dp))

        if (state.sent) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Solicitação enviada!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Acompanhe a resposta em \"Orçamentos\" na sua conta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Voltar") }
                }
            }
        } else {
            LcTextField(
                value = state.eventType,
                onValueChange = viewModel::onEventTypeChange,
                label = "Tipo de evento (ex.: Casamento)"
            )
            Spacer(Modifier.height(12.dp))
            LcTextField(
                value = state.eventDate,
                onValueChange = viewModel::onEventDateChange,
                label = "Data do evento (AAAA-MM-DD)",
                supportingText = "Opcional"
            )
            Spacer(Modifier.height(12.dp))
            LcTextField(
                value = state.city,
                onValueChange = viewModel::onCityChange,
                label = "Cidade do evento"
            )
            Spacer(Modifier.height(12.dp))
            LcTextField(
                value = state.budgetReais,
                onValueChange = viewModel::onBudgetChange,
                label = "Orçamento estimado (R$)",
                supportingText = "Opcional"
            )
            Spacer(Modifier.height(12.dp))
            LcTextField(
                value = state.message,
                onValueChange = viewModel::onMessageChange,
                label = "Mensagem",
                supportingText = "${state.message.trim().length}/3000 — mínimo de 10",
                minLines = 4,
                isError = state.message.trim().length > 3000
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(20.dp))
            LcButton(text = "Enviar solicitação", onClick = viewModel::submit, loading = state.sending)
        }
        Spacer(Modifier.height(32.dp))
    }
}
