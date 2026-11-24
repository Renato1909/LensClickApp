package com.lensclick.app.domain

data class Conversation(
    val id: Long,
    val counterpartName: String,
    val lastMessage: String,
    val lastMessageAt: String,
    val unreadCount: Int = 0,
    val photographerSlug: String = ""
)

data class Message(
    val id: Long,
    val senderId: Long,
    val text: String,
    val createdAt: String,
    val isMine: Boolean = false
)

interface ConversationsGateway {
    suspend fun list(): List<Conversation>
    suspend fun getMessages(conversationId: Long): List<Message>
    suspend fun sendMessage(conversationId: Long, text: String): Message
    suspend fun startConversation(photographerSlug: String, text: String): Conversation
    suspend fun markAsRead(conversationId: Long)
}
