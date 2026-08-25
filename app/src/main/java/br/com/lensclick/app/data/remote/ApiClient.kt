package br.com.lensclick.app.data.remote

import android.content.Context
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import br.com.lensclick.app.data.model.ApiError
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP único do app, apontando para a API de produção do LensClick.
 * Usa um CookieJar persistente porque a autenticação é por cookie de sessão.
 */
object ApiClient {

    const val BASE_URL = "https://lensclick.renato-aparecido-business.workers.dev"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    lateinit var client: HttpClient
        private set

    lateinit var cookieStorage: SharedPrefsCookiesStorage
        private set

    fun init(context: Context) {
        if (::client.isInitialized) return
        cookieStorage = SharedPrefsCookiesStorage(context.applicationContext)
        client = HttpClient {
            expectSuccess = false // tratamos status manualmente
            install(ContentNegotiation) { json(json) }
            UserAgent { agent = "LensClickApp/1.0 (Android)" }
            install(HttpCookies) {
                storage = cookieStorage
            }
        }
    }

    /**
     * Executa a chamada e devolve o corpo decodificado em caso de 2xx,
     * ou lança [ApiException] com a mensagem em PT vinda do backend.
     */
    suspend inline fun <reified T> request(block: HttpRequestBuilder.() -> Unit): T {
        val response = client.request(BASE_URL, block)
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            val message = runCatching { json.decodeFromString<ApiError>(bodyText).error }
                .getOrDefault("Falha na conexão (HTTP ${response.status.value}).")
            throw ApiException(message, response.status.value)
        }
        return json.decodeFromString(bodyText)
    }

    suspend inline fun <reified T> get(path: String): T =
        request { method = HttpMethod.Get; url(path) }

    suspend inline fun <reified T> post(path: String, body: Any? = null): T =
        request {
            method = HttpMethod.Post
            url(path)
            if (body != null) setBody(body)
        }
}

class ApiException(val statusCode: Int, message: String) : Exception(message)
