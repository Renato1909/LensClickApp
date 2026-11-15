package com.lensclick.app.domain

data class BudgetRequestDraft(
    val eventType: String,
    val eventDate: String,
    val place: String,
    val duration: String,
    val description: String,
    val additionalInfo: String
)

data class Proposal(
    val id: Long,
    val requestId: Long,
    val priceCents: Long,
    val conditions: String,
    val status: String,
    val createdAt: String
)

data class BudgetRequest(
    val id: Long,
    val eventType: String,
    val eventDate: String,
    val place: String,
    val duration: String,
    val description: String,
    val additionalInfo: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val counterpartName: String = "",
    val proposals: List<Proposal> = emptyList()
)

data class Favorite(
    val photographerId: Long,
    val slug: String,
    val name: String,
    val createdAt: String
)

// Consome /api/account/budget-requests*, /api/account/agenda com Bearer; regras e autorização são do servidor.
interface BudgetRequestsGateway {
    suspend fun create(slug: String, draft: BudgetRequestDraft): Long

    suspend fun list(): List<BudgetRequest>

    suspend fun detail(id: Long): BudgetRequest

    suspend fun propose(id: Long, priceCents: Long, conditions: String): Long

    suspend fun decide(id: Long, proposalId: Long, accept: Boolean)

    suspend fun cancel(id: Long)

    suspend fun agenda(): List<BudgetRequest>
}

// Consome /api/account/favorites* com Bearer.
interface FavoritesGateway {
    suspend fun list(): List<Favorite>

    suspend fun add(slug: String)

    suspend fun remove(photographerId: Long)
}