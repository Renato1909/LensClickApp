package br.com.lensclick.app.data.remote

import android.content.Context
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * CookiesStorage que persiste os cookies da sessão (lensclick_session)
 * em SharedPreferences, mantendo o usuário logado entre aberturas do app.
 */
class SharedPrefsCookiesStorage(context: Context) : CookiesStorage {

    private val prefs = context.getSharedPreferences("lensclick_cookies", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    @Serializable
    private data class StoredCookie(
        val name: String,
        val value: String,
        val domain: String,
        val path: String,
        val maxAge: Long
    )

    private val mapSerializer by lazy {
        MapSerializer(String.serializer(), StoredCookie.serializer())
    }

    private suspend fun load(): MutableMap<String, StoredCookie> =
        prefs.getString("entries", null)?.let { raw ->
            runCatching { json.decodeFromString(mapSerializer, raw) }.getOrNull()
        }?.toMutableMap() ?: mutableMapOf()

    private fun persist(entries: Map<String, StoredCookie>) {
        prefs.edit().putString("entries", json.encodeToString(mapSerializer, entries)).apply()
    }

    private fun key(host: String, name: String) = "$host|$name"

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val now = System.currentTimeMillis() / 1000
        return mutex.withLock {
            val entries = load()
            entries.values.filter { it.maxAge > now }.map {
                Cookie(name = it.name, value = it.value, domain = it.domain, path = it.path)
            }
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (!cookie.name.contains("session", ignoreCase = true)) return // só nos interessa a sessão
        mutex.withLock {
            val entries = load()
            val expiry = System.currentTimeMillis() / 1000 + (cookie.maxAge ?: DEFAULT_TTL_SECONDS)
            if ((cookie.maxAge ?: -1) == 0L) {
                // Expiração (logout): remove
                entries.remove(key(requestUrl.host, cookie.name))
            } else {
                entries[key(requestUrl.host, cookie.name)] = StoredCookie(
                    name = cookie.name,
                    value = cookie.value,
                    domain = cookie.domain ?: requestUrl.host,
                    path = cookie.path ?: "/",
                    maxAge = expiry
                )
            }
            persist(entries)
        }
    }

    override fun close() {}

    /** Limpa a sessão salva (usado no logout). */
    suspend fun clearSession() {
        mutex.withLock {
            prefs.edit().remove("entries").apply()
        }
    }

    companion object {
        // TTL do backend: 7 dias (SESSION_TTL_MS no worker).
        private const val DEFAULT_TTL_SECONDS = 7L * 24 * 60 * 60
    }
}
