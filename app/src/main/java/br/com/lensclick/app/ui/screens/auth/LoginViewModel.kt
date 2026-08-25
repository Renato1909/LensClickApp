package br.com.lensclick.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.data.remote.ApiException
import br.com.lensclick.app.data.remote.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val fieldError: String? = null
)

class LoginViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null, fieldError = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null, fieldError = null)
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return

        val fieldError = when {
            !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches() ->
                "Informe um e-mail válido."
            s.password.isBlank() -> "Informe sua senha."
            else -> null
        }
        if (fieldError != null) {
            _state.value = s.copy(fieldError = fieldError)
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(loading = true, error = null)
            try {
                val user = repo.login(s.email, s.password)
                SessionManager.setLoggedIn(user)
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
