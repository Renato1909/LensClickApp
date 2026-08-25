package br.com.lensclick.app.data.remote

import br.com.lensclick.app.data.model.ConversationsResponse
import br.com.lensclick.app.data.model.SendMessageResponse
import br.com.lensclick.app.data.model.ThreadResponse
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Repositório das conversas e mensagens entre cliente e fotógrafo. */
class MessagesRepository {

    /** Lista as conversas do usuário autenticado (cliente ou fotógrafo). */
    suspend fun listConversations(page: Int = 1): ConversationsResponse =
        ApiClient.get("/api/messages/conversations?page=$page")

    /**
     * Envia uma mensagem para [withUserId]. Se ainda não houver conversa
     * entre os dois usuários, ela é criada automaticamente.
     */
    suspend fun sendMessage(withUserId: Long, body: String): SendMessageResponse =
        ApiClient.request {
            method = HttpMethod.Post
            url("/api/messages")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("withUserId", withUserId)
                put("body", body)
            })
        }

    /** Lista as mensagens de uma conversa. Usa [beforeId] para paginar mensagens antigas. */
    suspend fun listThread(conversationId: Long, beforeId: Long? = null): ThreadResponse {
        val suffix = if (beforeId != null) "?beforeId=$beforeId" else ""
        return ApiClient.get("/api/messages/$conversationId$suffix")
    }
}
