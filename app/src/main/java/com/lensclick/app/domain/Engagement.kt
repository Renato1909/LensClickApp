package com.lensclick.app.domain

enum class PaymentMethod(val wire: String) {
    Cash("cash"), Pix("pix"), CreditCard("credit_card"), DebitCard("debit_card")
}

data class EngagementDraft(
    val category: String,
    val description: String,
    val eventType: String,
    val eventDate: String,
    val start: String,
    val end: String,
    val venue: String,
    val address: String,
    val city: String,
    val state: String,
    val venueDetails: String,
    val notes: String,
    val paymentMethod: PaymentMethod
)

data class Engagement(
    val id: Long,
    val slug: String,
    val clientName: String,
    val photographerName: String,
    val service: EngagementDraft,
    val priceCents: Long,
    val feeCents: Long,
    val paymentStatus: String,
    val status: String,
    val reviewPublished: Boolean,
    val createdAt: String
)

data class EngagementEvent(val status: String, val paymentStatus: String, val createdAt: String)
data class EngagementDetail(val engagement: Engagement, val history: List<EngagementEvent>)

data class EngagementOffer(
    val photographerName: String,
    val categories: List<String>,
    val priceCents: Long,
    val proposalId: Long? = null,
    val budget: BudgetRequest? = null
)

enum class EngagementAction(val wire: String) {
    Accept("accept"), Decline("decline"), Deliver("deliver"), Complete("complete"),
    Cancel("cancel"), ReceiveCash("receive_cash")
}

interface EngagementGateway {
    suspend fun create(
        slug: String,
        reference: String,
        priceCents: Long,
        proposalId: Long?,
        draft: EngagementDraft
    ): Engagement

    suspend fun list(): List<Engagement>
    suspend fun detail(id: Long): EngagementDetail
    suspend fun update(id: Long, action: EngagementAction): Engagement
    suspend fun review(id: Long, rating: Int, comment: String)
}
