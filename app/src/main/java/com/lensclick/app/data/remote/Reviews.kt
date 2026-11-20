package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.PublicReviews
import com.lensclick.app.domain.Review
import com.lensclick.app.domain.ReviewSummary
import com.lensclick.app.domain.ReviewsGateway
import org.json.JSONObject

class HttpReviewsGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore? = null
) : ReviewsGateway {

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
        val token = sessionStore?.readAccessToken()
            ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
        val body = JSONObject()
            .put("rating", rating)
            .put("comment", comment)
            .toString()
        val response = transport.execute(
            HttpRequest(
                method = "POST",
                path = "/api/photographers/$photographerSlug/reviews",
                headers = mapOf(
                    "Accept" to "application/json",
                    "Authorization" to "Bearer $token",
                    "Content-Type" to "application/json"
                ),
                body = body
            )
        )
        ensureHttpSuccess(response, 200, 201)
        return true
    }
}

fun parsePublicReviews(jsonStr: String): PublicReviews {
    val root = JSONObject(jsonStr)
    val summaryObj = root.optJSONObject("summary")
    val total = summaryObj?.optInt("total", 0) ?: 0
    val average = if (summaryObj == null || summaryObj.isNull("average")) null else summaryObj.getDouble("average")

    val reviewsArray = root.optJSONArray("reviews") ?: org.json.JSONArray()
    val list = (0 until reviewsArray.length()).map { index ->
        val row = reviewsArray.getJSONObject(index)
        Review(
            id = row.optLong("id"),
            authorName = row.optString("authorName", row.optString("clientName", "Cliente")),
            rating = row.optInt("rating", 5),
            comment = row.optString("comment", ""),
            createdAt = row.optString("createdAt", "")
        )
    }
    return PublicReviews(
        summary = ReviewSummary(total = total, average = average),
        reviews = list
    )
}
