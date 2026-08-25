package br.com.lensclick.app.ui.screens.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.lensclick.app.data.model.Quote
import br.com.lensclick.app.ui.components.ErrorMessage
import br.com.lensclick.app.ui.theme.Primary
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.Success
import br.com.lensclick.app.ui.theme.Surface
import br.com.lensclick.app.ui.theme.TextDim

private val STATUS_LABELS = mapOf(
    "pending" to "Pendente",
    "accepted" to "Aceito",
    "declined" to "Recusado"
)

/**
 * Caixas de orçamentos do usuário: enviadas (cliente) e recebidas (fotógrafo).
 */
@Composable
fun QuotesScreen(onBack: () -> Unit, initialBox: String = "sent") {
    val viewModel: QuotesViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.load(initialBox) }

    // Paginação infinita ao chegar perto do fim da lista.
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.quotes.isNotEmpty() && last >= state.quotes.size - 2 && state.page < state.totalPages
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadNextPage() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        androidx.compose.material3.TextButton(onClick = onBack) {
            Text("← Voltar", color = PrimaryLight)
        }
        Text(
            text = "Orçamentos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.box == "sent",
                onClick = { viewModel.load("sent") },
                label = { Text("Enviados") }
            )
            FilterChip(
                selected = state.box == "received",
                onClick = { viewModel.load("received") },
                label = { Text("Recebidos") }
            )
        }
        Spacer(Modifier.height(8.dp))

        state.actionMessage?.let {
            Text(it, color = Success, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
        }
        state.error?.let { ErrorMessage(text = it, modifier = Modifier.padding(bottom = 6.dp)) }

        when {
            state.loading -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryLight)
            }
            state.quotes.isEmpty() -> Text(
                text = if (state.box == "sent") "Você ainda não solicitou orçamentos."
                       else "Nenhuma solicitação recebida ainda.",
                color = TextDim,
                modifier = Modifier.padding(top = 24.dp)
            )
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp, top = 4.dp)
            ) {
                items(state.quotes, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        isReceivedBox = state.box == "received",
                        busy = state.busyQuoteId == quote.id,
                        onAccept = { viewModel.respond(quote.id, "accepted") },
                        onDecline = { viewModel.respond(quote.id, "declined") }
                    )
                }
                if (state.loadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryLight, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    isReceivedBox: Boolean,
    busy: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isReceivedBox) quote.clientName.ifBlank { "Cliente" } else quote.photographerName.ifBlank { "Fotógrafo" },
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = STATUS_LABELS[quote.status] ?: quote.status,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (quote.status) {
                        "accepted" -> Success
                        "declined" -> MaterialTheme.colorScheme.error
                        else -> TextDim
                    }
                )
            }
            if (quote.eventType.isNotBlank()) {
                Text(quote.eventType, style = MaterialTheme.typography.bodySmall, color = PrimaryLight)
            }
            val details = listOfNotNull(
                quote.eventDate.takeIf { it.isNotBlank() },
                quote.city.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (details.isNotBlank()) {
                Text(details, style = MaterialTheme.typography.bodySmall, color = TextDim)
            }
            quote.budgetCents?.let { cents ->
                val reais = cents / 100
                val rem = cents % 100
                Text(
                    text = "Orçamento estimado: R$ $reais.${rem.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }
            if (quote.message.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(quote.message, style = MaterialTheme.typography.bodyMedium)
            }
            if (isReceivedBox && quote.status == "pending") {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.weight(1f)
                    ) { Text("Aceitar") }
                    OutlinedButton(
                        onClick = onDecline,
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text("Recusar") }
                }
            }
        }
    }
}
