package br.com.lensclick.app.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lensclick.app.data.SessionManager
import br.com.lensclick.app.data.remote.AccountRepository
import br.com.lensclick.app.data.remote.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ---------- Dados pessoais ----------

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val loading: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val fieldError: String? = null
)

class EditProfileViewModel(private val repo: AccountRepository = AccountRepository()) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state

    fun load() {
        val user = SessionManager.user.value ?: return
        if (_state.name.isNotBlank()) return // já carregado
        _state.value = EditProfileUiState(name = user.name, email = user.email, phone = user.phone)
    }

    fun onNameChange(v: String) { update { it.copy(name = v) } }
    fun onEmailChange(v: String) { update { it.copy(email = v) } }
    fun onPhoneChange(v: String) {
        if (v.length <= 20 && v.all { c -> c.isDigit() || c in "+()- " }) update { it.copy(phone = v) }
    }

    private fun update(t: (EditProfileUiState) -> EditProfileUiState) {
        _state.value = t(_state.value).copy(error = null, fieldError = null, saved = false)
    }

    fun submit(currentPasswordForEmailChange: String?) {
        val s = _state.value
        if (s.loading) return
        when {
            s.name.trim().length < 2 -> return setField("Informe seu nome.")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> return setField("Informe um e-mail válido.")
            s.phone.filter { it.isDigit() }.length < 8 -> return setField("Informe um telefone válido com DDD.")
        }
        viewModelScope.launch {
            _state.value = s.copy(loading = true)
            try {
                val current = SessionManager.user.value!!
                val user = repo.updateProfile(
                    name = s.name.trim(),
                    email = s.email.trim(),
                    phone = s.phone.trim(),
                    role = current.role,
                    gender = current.gender ?: "other",
                    age = current.age,
                    profilePic = current.profilePic,
                    currentPassword = currentPasswordForEmailChange?.takeIf { it.isNotBlank() }
                )
                SessionManager.setLoggedIn(user)
                _state.value = _state.value.copy(loading = false, saved = true)
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }

    private fun setField(msg: String) { _state.value = _state.value.copy(fieldError = msg) }
}

// ---------- Alterar senha ----------

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val success: String? = null,
    val error: String? = null
)

class ChangePasswordViewModel(private val repo: AccountRepository = AccountRepository()) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state

    fun onCurrentChange(v: String) = update { it.copy(currentPassword = v) }
    fun onNewChange(v: String) = update { it.copy(newPassword = v) }
    fun onConfirmChange(v: String) = update { it.copy(confirmPassword = v) }

    private fun update(t: (ChangePasswordUiState) -> ChangePasswordUiState) {
        _state.value = t(_state.value).copy(error = null, success = null)
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        when {
            s.currentPassword.isBlank() -> return fail("Informe sua senha atual.")
            s.newPassword.length < 8 || !s.newPassword.any { it.isLetter() } || !s.newPassword.any { it.isDigit() } ->
                return fail("A nova senha deve ter ao menos 8 caracteres, com letra e número.")
            s.newPassword != s.confirmPassword -> return fail("As senhas não coincidem.")
        }
        viewModelScope.launch {
            _state.value = s.copy(loading = true)
            try {
                val response = repo.changePassword(s.currentPassword, s.newPassword)
                _state.value = _state.value.copy(loading = false, success = response.message.ifBlank { "Senha alterada com sucesso." })
                _state.value = _state.value.copy(currentPassword = "", newPassword = "", confirmPassword = "")
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }

    private fun fail(msg: String) { _state.value = _state.value.copy(error = msg) }
}

// ---------- Excluir conta ----------

data class DeleteAccountUiState(
    val password: String = "",
    val loading: Boolean = false,
    val done: Boolean = false,
    val error: String? = null
)

class DeleteAccountViewModel(private val repo: AccountRepository = AccountRepository()) : ViewModel() {

    private val _state = MutableStateFlow(DeleteAccountUiState())
    val state: StateFlow<DeleteAccountUiState> = _state

    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v, error = null) }

    fun submit(onDone: () -> Unit) {
        val s = _state.value
        if (s.loading || s.password.isBlank()) {
            if (s.password.isBlank()) _state.value = s.copy(error = "Informe sua senha para confirmar.")
            return
        }
        viewModelScope.launch {
            _state.value = s.copy(loading = true)
            try {
                repo.deleteAccount(s.password)
                SessionManager.setLoggedOut()
                _state.value = DeleteAccountUiState(done = true)
                onDone()
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Não foi possível conectar. Verifique sua internet.")
            }
        }
    }
}
