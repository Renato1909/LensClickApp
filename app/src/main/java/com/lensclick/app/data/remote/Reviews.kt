package com.lensclick.app.data.remote

import com.lensclick.app.ui.localized
import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.PublicReviews
import com.lensclick.app.domain.Review
import com.lensclick.app.domain.ReviewRequest
import com.lensclick.app.domain.ReviewSummary
import com.lensclick.app.domain.ReviewsGateway
import org.json.JSONObject

class HttpReviewsGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore? = null
) : ReviewsGateway {

    private fun authenticatedStore() = sessionStore
        ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)

    override suspend fun getReviews(photographerSlug: String): PublicReviews {
        val response = transport.execute(
            HttpRequest(
                method = "GET",
                path = "/api/photographers/$photographerSlug/reviews",
                headers = mapOf("Accept" to "application/json")
            )
        )
        ensureHttpSuccess(response, 200)
        return parsePublicReviews(response.body)
    }

    override suspend fun submitReview(photographerSlug: String, rating: Int, comment: String): Boolean {
        val body = JSONObject()
            .put("rating", rating)
            .put("comment", comment)
            .toString()
        val response = transport.execute(
            authenticatedStore().bearerRequest("POST", "/api/photographers/$photographerSlug/reviews", body)
        )
        ensureHttpSuccess(response, 200, 201)
        return true
    }

    override suspend fun requestVerification(photographerSlug: String, message: String) {
        val body = JSONObject().put("message", message).toString()
        val response = transport.execute(
            authenticatedStore().bearerRequest("POST", "/api/photographers/$photographerSlug/review-requests", body)
        )
        ensureHttpSuccess(response, 201)
    }

    override suspend fun listReviewRequests(): List<ReviewRequest> {
        val response = transport.execute(authenticatedStore().bearerRequest("GET", "/api/account/review-requests"))
        ensureHttpSuccess(response, 200)
        val rows = JSONObject(response.body).getJSONArray("requests")
        return (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            ReviewRequest(
                id = row.getLong("id"),
                counterpartName = row.optString("clientName").ifBlank { row.optString("photographerName") },
                message = row.optString("message"),
                status = row.getString("status")
            )
        }
    }

    override suspend fun resolveReviewRequest(id: Long, confirm: Boolean) {
        val body = JSONObject().put("status", if (confirm) "confirmed" else "rejected").toString()
        val response = transport.execute(
            authenticatedStore().bearerRequest("PUT", "/api/account/review-requests/$id", body)
        )
        ensureHttpSuccess(response, 200)
    }
}

fun parsePublicReviews(jsonStr: String): PublicReviews {
    val root = JSONObject(jsonStr)
    val summaryObj = root.optJSONObject("summary")
    val total = summaryObj?.optInt("total", 0) ?: 0
    val average = if (summaryObj == null || summaryObj.isNull("average")) null else summaryObj.getDouble("average")
    val distribution = root.optJSONObject("distribution")?.let { counts ->
        (1..5).associateWith { counts.getInt(it.toString()) }
    }

    val reviewsArray = root.optJSONArray("reviews") ?: org.json.JSONArray()
    val list = (0 until reviewsArray.length()).map { index ->
        val row = reviewsArray.getJSONObject(index)
        Review(
            id = row.optLong("id"),
            authorName = row.optString("authorName", row.optString("clientName", localized("Cliente"))),
            rating = row.getInt("rating").also { require(it in 1..5) },
            comment = row.optString("comment", ""),
            createdAt = row.optString("createdAt", "")
        )
    }
    return PublicReviews(
        summary = ReviewSummary(total = total, average = average, distribution = distribution),
        reviews = list
    )
}
