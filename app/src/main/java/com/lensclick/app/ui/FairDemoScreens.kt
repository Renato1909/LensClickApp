package com.lensclick.app.ui

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.ui.theme.LensMuted
import org.json.JSONArray
import org.json.JSONObject

internal data class FairDemoQuote(val photographer: String, val draft: BudgetRequestDraft)

internal class FairDemoStore(context: Context) {
    private val preferences = context.getSharedPreferences("lensclick_fair_demo", Context.MODE_PRIVATE)

    var favorites by mutableStateOf(preferences.getStringSet("favorites", emptySet()).orEmpty().toSet())
        private set

    fun toggleFavorite(slug: String) {
        favorites = if (slug in favorites) favorites - slug else favorites + slug
        preferences.edit().putStringSet("favorites", favorites).apply()
    }

    fun messages(slug: String): List<String> {
        val stored = read("chat/$slug")
        return List(stored.length(), stored::getString)
    }

    fun sendMessage(slug: String, message: String) {
        val key = "chat/$slug"
        preferences.edit().putString(key, read(key).put(message).toString()).apply()
    }

    fun recordQuote(photographer: String, draft: BudgetRequestDraft) {
        val quote = JSONObject().put("photographer", photographer)
            .put("eventType", draft.eventType).put("eventDate", draft.eventDate).put("place", draft.place)
            .put("duration", draft.duration).put("description", draft.description).put("details", draft.additionalInfo)
        preferences.edit().putString("quotes", read("quotes").put(quote).toString()).apply()
    }

    fun quotes(): List<FairDemoQuote> {
        val stored = read("quotes")
        return (stored.length() - 1 downTo 0).map { index ->
            val quote = stored.getJSONObject(index)
            FairDemoQuote(
                quote.getString("photographer"),
                BudgetRequestDraft(
                    quote.getString("eventType"), quote.getString("eventDate"), quote.getString("place"),
                    quote.getString("duration"), quote.getString("description"), quote.getString("details")
                )
            )
        }
    }

    private fun read(key: String) = JSONArray(preferences.getString(key, "[]"))
}

@Composable
internal fun FairDemoChatScreen(name: String, slug: String, store: FairDemoStore, onBack: () -> Unit) {
    var messages by remember(store, slug) { mutableStateOf(store.messages(slug)) }
    var input by rememberSaveable(slug) { mutableStateOf("") }
    SettingsPage(localized("Conversar com {0}", name), onBack) {
        Text(localized("Conversa fictícia. As mensagens ficam neste aparelho."), color = LensMuted)
        if (messages.isEmpty()) {
            Text(localized("Nenhuma mensagem nesta conversa."), color = LensMuted)
        }
        messages.forEach { Bubble(it, mine = true) }
        LensField(localized("Mensagem"), input, { input = it }, singleLine = false, maxLines = 4)
        PrimaryButton(localized("Enviar"), enabled = input.trim().length in 1..2_000, onClick = {
            val message = input.trim()
            store.sendMessage(slug, message)
            messages = messages + message
            input = ""
        })
    }
}
