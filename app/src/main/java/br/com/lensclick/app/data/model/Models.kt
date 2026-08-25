package br.com.lensclick.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val age: Long? = null,
    val gender: String? = null,
    val profilePic: String = ""
)

@Serializable
data class UserResponse(val user: User)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class ApiError(val error: String = "Erro inesperado.")

// ---------- Busca de fotógrafos ----------

@Serializable
data class PhotographerContacts(val email: String = "", val phone: String = "")

@Serializable
data class Photographer(
    val slug: String,
    val name: String,
    val bio: String = "",
    val specialties: List<String> = emptyList(),
    val city: String = "",
    val state: String = "",
    @SerialName("serviceRadiusKm") val serviceRadiusKm: Double? = null,
    @SerialName("startingPriceCents") val startingPriceCents: Long? = null,
    val availability: String = "available",
    val instagram: String = "",
    val website: String = "",
    @SerialName("isPublished") val isPublished: Boolean = true,
    val profilePic: String = "",
    val contacts: PhotographerContacts = PhotographerContacts()
)

@Serializable
data class Pagination(
    val page: Int,
    @SerialName("pageSize") val pageSize: Int,
    val total: Int,
    @SerialName("totalPages") val totalPages: Int
)

@Serializable
data class SearchResponse(
    val photographers: List<Photographer>,
    val pagination: Pagination
)

@Serializable
data class PortfolioItem(
    val id: Long,
    val caption: String = "",
    val altText: String = "",
    val position: Int = 0,
    val imageUrl: String = "",
    @SerialName("updated_at") val updatedAt: Long? = null
)

@Serializable
data class PhotographerDetail(
    val slug: String,
    val name: String,
    @SerialName("userId") val userId: Long = 0,
    val bio: String = "",
    val specialties: List<String> = emptyList(),
    val city: String = "",
    val state: String = "",
    @SerialName("serviceRadiusKm") val serviceRadiusKm: Double? = null,
    @SerialName("startingPriceCents") val startingPriceCents: Long? = null,
    val availability: String = "available",
    val instagram: String = "",
    val website: String = "",
    @SerialName("isPublished") val isPublished: Boolean = true,
    val profilePic: String = "",
    val contacts: PhotographerContacts = PhotographerContacts(),
    val portfolio: List<PortfolioItem> = emptyList()
)

@Serializable
data class PhotographerDetailResponse(val profile: PhotographerDetail)

// ---------- Orçamentos ----------

@Serializable
data class Quote(
    val id: Long,
    val clientId: Long = 0,
    val clientName: String = "",
    val photographerId: Long = 0,
    val photographerName: String = "",
    val photographerSlug: String = "",
    val eventType: String = "",
    val eventDate: String = "",
    val city: String = "",
    val budgetCents: Long? = null,
    val message: String = "",
    val status: String = "pending",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class QuotesResponse(
    val quotes: List<Quote> = emptyList(),
    val pagination: Pagination? = null
)

// ---------- Mensagens ----------

@Serializable
data class Conversation(
    val id: Long,
    val clientId: Long = 0,
    val clientName: String = "",
    val photographerId: Long = 0,
    val photographerName: String = "",
    val photographerSlug: String = "",
    val otherUserId: Long = 0,
    val otherUserName: String = "",
    val lastMessageBody: String = "",
    val lastMessageAt: String = "",
    val messageCount: Int = 0
)

@Serializable
data class ConversationsResponse(
    val conversations: List<Conversation> = emptyList(),
    val pagination: Pagination? = null
)

@Serializable
data class Message(
    val id: Long,
    val conversationId: Long = 0,
    val senderId: Long = 0,
    val body: String = "",
    val createdAt: String = ""
)

@Serializable
data class ThreadResponse(
    val conversationId: Long,
    val clientId: Long = 0,
    val photographerId: Long = 0,
    val messages: List<Message> = emptyList(),
    val hasMore: Boolean = false
)

@Serializable
data class SendMessageResponse(
    val message: String = "",
    val conversationId: Long = 0
)
