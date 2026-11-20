package com.lensclick.app.domain

data class ReviewSummary(
    val total: Int,
    val average: Double?
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

interface ReviewsGateway {
    suspend fun getReviews(photographerSlug: String): PublicReviews
    suspend fun submitReview(photographerSlug: String, rating: Int, comment: String): Boolean
}
