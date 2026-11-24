package com.lensclick.app.domain

data class ReviewSummary(
    val total: Int,
    val average: Double?,
    val distribution: Map<Int, Int>? = null
)

data class Review(
    val id: Long,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class PublicReviews(
    val summary: ReviewSummary,
    val reviews: List<Review>
)

data class ReviewRequest(
    val id: Long,
    val counterpartName: String,
    val message: String,
    val status: String
)

interface ReviewsGateway {
    suspend fun getReviews(photographerSlug: String): PublicReviews
    suspend fun submitReview(photographerSlug: String, rating: Int, comment: String): Boolean
    suspend fun requestVerification(photographerSlug: String, message: String)
    suspend fun listReviewRequests(): List<ReviewRequest>
    suspend fun resolveReviewRequest(id: Long, confirm: Boolean)
}
