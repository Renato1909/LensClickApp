package br.com.lensclick.app.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.data.model.Message
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.MessagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ThreadUiState(
    val messages: List<Message> = emptyList(),
    val otherUserName: String = "",
    val loading: Boolean = true,
    val sending: Boolean = false,
    val draft: String = "",
    val error: String? = null
)

/**
 * Conversa 1:1 entre cliente e fotógrafo. [withUserId] é o outro participante;
 * se a conversa ainda não existir, a primeira mensagem cria o thread no backend.
 */
class ThreadViewModel(private val withUserId: Long, private val otherUserName: String) : ViewModel() {
    private val repository = MessagesRepository()
    private val _state = MutableStateFlow(ThreadUiState(otherUserName = otherUserName))
    val state: StateFlow<ThreadUiState> = _state.asStateFlow()

    private var conversationId: Long? = null

    fun onDraftChange(value: String) {
        if (value.length <= 2000) {
            _state.value = _state.value.copy(draft = value)
        }
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                // Busca a conversa existente na lista (o backend não expõe lookup por par).
                val conversations = repository.listConversations(page = 1).conversations
                val match = conversations.firstOrNull { it.otherUserId == withUserId }
                conversationId = match?.id
                val messages = match?.let { repository.listThread(it.id).messages } ?: emptyList()
                _state.value = _state.value.copy(
                    messages = messages,
                    otherUserName = match?.otherUserName?.ifBlank { otherUserName } ?: otherUserName,
                    loading = false
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Não foi possível conectar. Verifique sua internet."
                )
            }
        }
    }

    fun send() {
        val current = _state.value
        val text = current.draft.trim()
        if (text.isEmpty() || current.sending) return
        _state.value = current.copy(sending = true, error = null)
        viewModelScope.launch {
            try {
                val response = repository.sendMessage(withUserId, text)
                conversationId = response.conversationId.takeIf { it > 0 } ?: conversationId
                val fresh = conversationId?.let { repository.listThread(it).messages } ?: emptyList()
                _state.value = _state.value.copy(
                    messages = fresh,
                    draft = "",
                    sending = false
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(sending = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    sending = false,
                    error = "Não foi possível enviar. Verifique sua internet."
                )
            }
        }
    }
}
