package br.com.lensclick.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val fieldError: String? = null,
    val done: Boolean = false
)

class RegisterViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun onNameChange(v: String) { update { it.copy(name = v) } }
    fun onEmailChange(v: String) { update { it.copy(email = v) } }
    fun onPhoneChange(v: String) {
        // aceita apenas dígitos, +, espaço, () e - (máx 20 chars como o backend)
        if (v.length <= 20 && v.all { c -> c.isDigit() || c in "+()- " }) update { it.copy(phone = v) }
    }
    fun onPasswordChange(v: String) { update { it.copy(password = v) } }

    private fun update(transform: (RegisterUiState) -> RegisterUiState) {
        _state.value = _state.value.let(transform).copy(error = null, fieldError = null)
    }

    /** Mesmas regras do backend (worker.mjs): nome 2+, e-mail válido, telefone 8+ dígitos, senha 8+ com letra e número. */
    fun validate(): String? {
        val s = _state.value
        return when {
            s.name.trim().length < 2 -> "Informe seu nome completo."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> "Informe um e-mail válido."
            s.phone.filter { it.isDigit() }.length < 8 -> "Informe um telefone válido com DDD."
            s.password.length < 8 -> "A senha deve ter pelo menos 8 caracteres."
            !s.password.any { it.isLetter() } || !s.password.any { it.isDigit() } ->
                "A senha deve conter letras e números."
            else -> null
        }
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        val fieldError = validate()
        if (fieldError != null) {
            _state.value = s.copy(fieldError = fieldError)
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(loading = true, error = null)
            try {
                val user = repo.register(s.name, s.email, s.phone, s.password)
                SessionManager.setLoggedIn(user)
                _state.value = _state.value.copy(loading = false, done = true)
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
}
