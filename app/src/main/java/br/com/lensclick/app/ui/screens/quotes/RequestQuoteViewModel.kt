package br.com.lensclick.app.ui.screens.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.QuotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RequestQuoteUiState(
    val eventType: String = "",
    val eventDate: String = "",
    val city: String = "",
    val budgetReais: String = "",
    val message: String = "",
    val sending: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)

/** Formulário "Solicitar orçamento" para um fotógrafo específico (slug). */
class RequestQuoteViewModel(private val slug: String) : ViewModel() {

    private val repo = QuotesRepository()
    private var budgetCentsValue: Long? = null

    private val _state = MutableStateFlow(RequestQuoteUiState())
    val state: StateFlow<RequestQuoteUiState> = _state

    fun onEventTypeChange(v: String) { if (v.length <= 50) update { it.copy(eventType = v) } }

    fun onEventDateChange(v: String) {
        if (!v.all(Char::isDigit) && !v.matches(Regex("\\d{0,4}(-\\d{0,2})?(-\\d{0,2})?"))) return
        update { it.copy(eventDate = v) }
    }

    fun onCityChange(v: String) { if (v.length <= 80) update { it.copy(city = v) } }

    /** Aceita "1500", "1500,50" ou "1500.50" e converte para centavos. */
    fun onBudgetChange(v: String) {
        if (!v.matches(Regex("\\d{0,7}([.,]\\d{0,2})?"))) return
        budgetCentsValue = v.replace(",", ".")
            .split(".")
            .let { parts ->
                val reais = parts[0].toLongOrNull() ?: 0L
                val cents = when (parts.getOrNull(1)) {
                    null -> 0L
                    else -> parts[1].padEnd(2, '0').toLongOrNull() ?: 0L
                }
                if (reais > 9_999_999) null else reais * 100 + cents
            }
        update { it.copy(budgetReais = v) }
    }

    fun onMessageChange(v: String) { if (v.length <= 3000) update { it.copy(message = v) } }

    private fun update(t: (RequestQuoteUiState) -> RequestQuoteUiState) {
        _state.value = t(_state.value).copy(error = null)
    }

    fun submit() {
        val s = _state.value
        if (s.sending) return
        when {
            s.message.trim().length < 10 ->
                return fail("Descreva o serviço com pelo menos 10 caracteres.")
            s.eventDate.isNotBlank() && !Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(s.eventDate) ->
                return fail("Informe a data no formato AAAA-MM-DD.")
        }
        viewModelScope.launch {
            _state.value = s.copy(sending = true)
            try {
                repo.requestQuote(
                    slug = slug,
                    eventType = s.eventType.trim(),
                    eventDate = s.eventDate.trim(),
                    city = s.city.trim(),
                    budgetCents = if (s.budgetReais.isBlank()) null else budgetCentsValue,
                    message = s.message.trim()
                )
                _state.value = _state.value.copy(sending = false, sent = true)
            } catch (e: ApiException) {
                _state.value = _state.value.copy(sending = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(sending = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }

    private fun fail(msg: String) { _state.value = _state.value.copy(error = msg, sending = false) }
}
