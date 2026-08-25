package br.com.lensclick.app.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.model.Conversation
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.MessagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val loading: Boolean = true,
    val page: Int = 1,
    val totalPages: Int = 1,
    val loadingMore: Boolean = false,
    val error: String? = null
)

/** Lista de conversas do usuário autenticado (cliente ou fotógrafo). */
class ConversationsViewModel : ViewModel() {
    private val repository = MessagesRepository()
    private val _state = MutableStateFlow(ConversationsUiState())
    val state: StateFlow<ConversationsUiState> = _state.asStateFlow()

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null, page = 1, totalPages = 1)
        viewModelScope.launch {
            try {
                val response = repository.listConversations(page = 1)
                _state.value = _state.value.copy(
                    conversations = response.conversations,
                    loading = false,
                    page = 1,
                    totalPages = response.pagination?.totalPages ?: 1
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

    fun loadNextPage() {
        val current = _state.value
        if (current.loadingMore || current.page >= current.totalPages) return
        _state.value = current.copy(loadingMore = true)
        viewModelScope.launch {
            try {
                val nextPage = current.page + 1
                val response = repository.listConversations(page = nextPage)
                _state.value = _state.value.copy(
                    conversations = (_state.value.conversations + response.conversations)
                        .distinctBy { it.id },
                    page = nextPage,
                    totalPages = response.pagination?.totalPages ?: _state.value.totalPages,
                    loadingMore = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loadingMore = false)
            }
        }
    }
}
