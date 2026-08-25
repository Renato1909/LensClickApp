package br.com.lensclick.app.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.ui.components.ErrorMessage
import br.com.lensclick.app.ui.theme.Primary
import br.com.lensclick.app.ui.theme.PrimaryLight
import br.com.lensclick.app.ui.theme.SurfaceHigh

/**
 * Thread de mensagens 1:1 com o outro usuário ([withUserId]).
 * Se a conversa não existir ainda, a primeira mensagem cria o thread.
 */
@Composable
fun ThreadScreen(withUserId: Long, otherUserName: String, onBack: () -> Unit) {
    val viewModel: ThreadViewModel = viewModel(
        key = "thread-$withUserId",
        factory = viewModelFactory { initializer { ThreadViewModel(withUserId, otherUserName) } }
    )
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val myId = SessionManager.user.value?.id ?: 0L

    LaunchedEffect(Unit) { viewModel.load() }

    // Rola para a última mensagem sempre que a lista muda.
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        TextButton(onClick = onBack) { Text("← Voltar", color = PrimaryLight) }
        Text(
            text = state.otherUserName.ifBlank { "Conversa" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryLight,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryLight)
            }
            else -> {
                state.error?.let { ErrorMessage(text = it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageBubble(mine = message.senderId == myId, message = message)
                    }
                    if (state.messages.isEmpty()) {
                        item {
                            Text(
                                text = "Envie a primeira mensagem.",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.draft,
                        onValueChange = viewModel::onDraftChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escreva uma mensagem...") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                    Button(
                        onClick = viewModel::send,
                        enabled = !state.sending && state.draft.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (state.sending) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(mine: Boolean, message: br.com.lensclick.app.data.model.Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (mine) 14.dp else 2.dp,
                bottomEnd = if (mine) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (mine) Primary else SurfaceHigh
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
