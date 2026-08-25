package br.com.lensclick.app.data

import br.com.lensclick.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sessão em memória do app. `user == null` significa deslogado.
 * Alimentada pelo /api/auth/me na inicialização e pelas telas de login/cadastro.
 */
object SessionManager {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun startLoading() {
        _loading.value = true
    }

    fun setLoggedIn(user: User) {
        _user.value = user
        _loading.value = false
    }

    fun setLoggedOut() {
        _user.value = null
        _loading.value = false
    }
}
