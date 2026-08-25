package br.com.lensclick.app.ui.screens.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.model.Quote
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.QuotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuotesUiState(
    val box: String = "sent", // "sent" | "received"
    val quotes: List<Quote> = emptyList(),
    val loading: Boolean = true,
    val page: Int = 1,
    val totalPages: Int = 1,
    val loadingMore: Boolean = false,
    val busyQuoteId: Long? = null, // orçamento sendo aceito/recusado agora
    val error: String? = null,
    val actionMessage: String? = null
)

/**
 * Caixas de orçamentos: enviadas (cliente) e recebidas (fotógrafo).
 * No box "received" o fotógrafo pode aceitar/recusar pedidos pendentes.
 */
class QuotesViewModel : ViewModel() {

    private val repo = QuotesRepository()

    private val _state = MutableStateFlow(QuotesUiState())
    val state: StateFlow<QuotesUiState> = _state

    fun load(box: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(box = box, loading = true, error = null, page = 1)
            try {
                val response = repo.listQuotes(box, page = 1)
                _state.value = _state.value.copy(
                    loading = false,
                    quotes = response.quotes,
                    totalPages = response.pagination?.totalPages ?: 1
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }

    fun loadNextPage() {
        val s = _state.value
        if (s.loadingMore || s.page >= s.totalPages) return
        viewModelScope.launch {
            _state.value = s.copy(loadingMore = true)
            try {
                val response = repo.listQuotes(s.box, page = s.page + 1)
                _state.value = _state.value.copy(
                    loadingMore = false,
                    page = s.page + 1,
                    quotes = (_state.value.quotes + response.quotes).distinctBy { it.id },
                    totalPages = response.pagination?.totalPages ?: s.totalPages
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loadingMore = false)
            }
        }
    }

    /** Fotógrafo responde um pedido pendente: accepted ou declined. */
    fun respond(quoteId: Long, status: String) {
        val s = _state.value
        if (s.busyQuoteId != null) return
        viewModelScope.launch {
            _state.value = s.copy(busyQuoteId = quoteId, actionMessage = null, error = null)
            try {
                val result = repo.respondQuote(quoteId, status)
                _state.value = _state.value.copy(
                    busyQuoteId = null,
                    actionMessage = result.message.ifBlank { if (status == "accepted") "Orçamento aceito." else "Orçamento recusado." },
                    quotes = _state.value.quotes.map { q ->
                        if (q.id == quoteId) q.copy(status = status) else q
                    }
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(busyQuoteId = null, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(busyQuoteId = null, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }
}
