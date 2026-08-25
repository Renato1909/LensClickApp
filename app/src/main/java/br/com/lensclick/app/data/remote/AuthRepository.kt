package br.com.lensclick.app.data.remote

import br.com.lensclick.app.data.model.LoginRequest
import br.com.lensclick.app.data.model.RegisterRequest
import br.com.lensclick.app.data.model.User
import br.com.lensclick.app.data.model.UserResponse

/**
 * Operações de autenticação contra a API do LensClick.
 * A sessão é mantida automaticamente pelo cookie persistido no ApiClient.
 */
class AuthRepository {

    suspend fun register(name: String, email: String, phone: String, password: String): User =
        ApiClient.post(
            "/api/auth/register",
            RegisterRequest(name = name.trim(), email = email.trim(), phone = phone.trim(), password = password)
        )

    suspend fun login(email: String, password: String): User =
        ApiClient.post("/api/auth/login", LoginRequest(email = email.trim(), password = password))

    suspend fun me(): User? = runCatching {
        ApiClient.get<UserResponse>("/api/auth/me").user
    }.getOrNull()

    suspend fun logout() {
        runCatching { ApiClient.post<Unit>("/api/auth/logout") }
    }
}
