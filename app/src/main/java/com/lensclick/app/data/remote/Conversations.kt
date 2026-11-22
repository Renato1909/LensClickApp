package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.Conversation
import com.lensclick.app.domain.ConversationsGateway
import com.lensclick.app.domain.Message
import org.json.JSONObject

class HttpConversationsGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : ConversationsGateway {

    override suspend fun list(): List<Conversation> {
        val response = transport.execute(sessionStore.bearerRequest("GET", "/api/account/conversations"))
        ensureHttpSuccess(response, 200)
        val json = JSONObject(response.body)
        val rows = json.optJSONArray("conversations") ?: return emptyList()
        return (0 until rows.length()).map { parseConversation(rows.getJSONObject(it)) }
    }

    override suspend fun getMessages(conversationId: Long): List<Message> {
        val response = transport.execute(sessionStore.bearerRequest("GET", "/api/account/conversations/$conversationId/messages"))
        ensureHttpSuccess(response, 200)
        val json = JSONObject(response.body)
        val rows = json.optJSONArray("messages") ?: return emptyList()
        return (0 until rows.length()).map { parseMessage(rows.getJSONObject(it)) }
    }

    override suspend fun sendMessage(conversationId: Long, text: String): Message {
        val body = JSONObject().put("message", text).toString()
        val response = transport.execute(sessionStore.bearerRequest("POST", "/api/account/conversations/$conversationId/messages", body))
        ensureHttpSuccess(response, 200, 201)
        val json = JSONObject(response.body)
        val msgObj = json.optJSONObject("message") ?: json
        return parseMessage(msgObj)
    }

    override suspend fun startConversation(photographerSlug: String, text: String): Conversation {
        val body = JSONObject().put("message", text).toString()
        val response = transport.execute(sessionStore.bearerRequest("POST", "/api/photographers/$photographerSlug/conversations", body))
        ensureHttpSuccess(response, 200, 201)
        val json = JSONObject(response.body)
        val convObj = json.optJSONObject("conversation") ?: json
        return parseConversation(convObj)
    }

    override suspend fun markAsRead(conversationId: Long) {
        val response = transport.execute(sessionStore.bearerRequest("PUT", "/api/account/conversations/$conversationId/read"))
        ensureHttpSuccess(response, 200)
    }
}

fun parseConversation(json: JSONObject): Conversation = Conversation(
    id = json.getLong("id"),
    counterpartName = json.optString("counterpartName").ifBlank { json.optString("photographerName", "Fotógrafo") },
    lastMessage = json.optString("lastMessage", ""),
    lastMessageAt = json.optString("lastMessageAt", json.optString("updatedAt", "")),
    unreadCount = json.optInt("unreadCount", 0)
)

fun parseMessage(json: JSONObject): Message = Message(
    id = json.optLong("id"),
    senderId = json.optLong("senderId"),
    text = json.optString("text", json.optString("message", "")),
    createdAt = json.optString("createdAt"),
    isMine = json.optBoolean("isMine", false)
)
