package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.BudgetRequest
import com.lensclick.app.domain.BudgetRequestDraft
import com.lensclick.app.domain.BudgetRequestsGateway
import com.lensclick.app.domain.Favorite
import com.lensclick.app.domain.FavoritesGateway
import com.lensclick.app.domain.Proposal
import org.json.JSONObject

class HttpBudgetRequestsGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : BudgetRequestsGateway {
    override suspend fun create(slug: String, draft: BudgetRequestDraft): Long {
        val body = JSONObject()
            .put("eventType", draft.eventType)
            .put("eventDate", draft.eventDate)
            .put("place", draft.place)
            .put("duration", draft.duration)
            .put("description", draft.description)
            .put("additionalInfo", draft.additionalInfo)
            .toString()
        val response = transport.execute(bearerRequest("POST", "/api/photographers/$slug/budget-requests", body))
        ensureHttpSuccess(response, 201)
        return parseCreatedId(response.body, "request")
    }

    override suspend fun list(): List<BudgetRequest> {
        val response = transport.execute(bearerRequest("GET", "/api/account/budget-requests"))
        ensureHttpSuccess(response, 200)
        return parseBudgetRequests(response.body, "requests")
    }

    override suspend fun detail(id: Long): BudgetRequest {
        val response = transport.execute(bearerRequest("GET", "/api/account/budget-requests/$id"))
        ensureHttpSuccess(response, 200)
        return parseBudgetRequest(JSONObject(response.body).getJSONObject("request"))
    }

    override suspend fun propose(id: Long, priceCents: Long, conditions: String): Long {
        val body = JSONObject().put("priceCents", priceCents).put("conditions", conditions).toString()
        val response = transport.execute(bearerRequest("POST", "/api/account/budget-requests/$id/proposals", body))
        ensureHttpSuccess(response, 201)
        return parseCreatedId(response.body, "proposal")
    }

    override suspend fun decide(id: Long, proposalId: Long, accept: Boolean) {
        val body = JSONObject().put("action", if (accept) "accept" else "reject").put("proposalId", proposalId).toString()
        val response = transport.execute(bearerRequest("PUT", "/api/account/budget-requests/$id", body))
        ensureHttpSuccess(response, 200)
    }

    override suspend fun cancel(id: Long) {
        val body = JSONObject().put("action", "cancel").toString()
        val response = transport.execute(bearerRequest("PUT", "/api/account/budget-requests/$id", body))
        ensureHttpSuccess(response, 200)
    }

    override suspend fun agenda(): List<BudgetRequest> {
        val response = transport.execute(bearerRequest("GET", "/api/account/agenda"))
        ensureHttpSuccess(response, 200)
        return parseBudgetRequests(response.body, "events")
    }

    private fun bearerRequest(method: String, path: String, body: String? = null): HttpRequest {
        val token = sessionStore.readAccessToken()
            ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
        val headers = buildMap {
            put("Accept", "application/json")
            put("Authorization", "Bearer $token")
            if (body != null) put("Content-Type", "application/json")
        }
        return HttpRequest(method, path, headers, body)
    }
}

class HttpFavoritesGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : FavoritesGateway {
    override suspend fun list(): List<Favorite> {
        val response = transport.execute(bearerRequest("GET", "/api/account/favorites"))
        ensureHttpSuccess(response, 200)
        val rows = JSONObject(response.body).getJSONArray("favorites")
        return (0 until rows.length()).map { parseFavorite(rows.getJSONObject(it)) }
    }

    override suspend fun add(slug: String) {
        val response = transport.execute(bearerRequest("POST", "/api/account/favorites", JSONObject().put("slug", slug).toString()))
        ensureHttpSuccess(response, 201)
    }

    override suspend fun remove(photographerId: Long) {
        val response = transport.execute(bearerRequest("DELETE", "/api/account/favorites/$photographerId"))
        ensureHttpSuccess(response, 200)
    }

    private fun bearerRequest(method: String, path: String, body: String? = null): HttpRequest {
        val token = sessionStore.readAccessToken()
            ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
        val headers = buildMap {
            put("Accept", "application/json")
            put("Authorization", "Bearer $token")
            if (body != null) put("Content-Type", "application/json")
        }
        return HttpRequest(method, path, headers, body)
    }
}

private fun parseCreatedId(body: String, key: String): Long = JSONObject(body).getJSONObject(key).getLong("id")

private fun parseBudgetRequests(body: String, key: String): List<BudgetRequest> {
    val rows = JSONObject(body).getJSONArray(key)
    return (0 until rows.length()).map { parseBudgetRequest(rows.getJSONObject(it)) }
}

fun parseProposal(row: JSONObject): Proposal = Proposal(
    id = row.getLong("id"),
    requestId = row.optLong("requestId"),
    priceCents = row.optLong("priceCents"),
    conditions = row.optString("conditions"),
    status = row.optString("status"),
    createdAt = row.optString("createdAt")
)

fun parseBudgetRequest(row: JSONObject): BudgetRequest = BudgetRequest(
    id = row.getLong("id"),
    eventType = row.optString("eventType"),
    eventDate = row.optString("eventDate"),
    place = row.optString("place"),
    duration = row.optString("duration"),
    description = row.optString("description"),
    additionalInfo = row.optString("additionalInfo"),
    status = row.optString("status"),
    createdAt = row.optString("createdAt"),
    updatedAt = row.optString("updatedAt"),
    counterpartName = row.optString("counterpartName").ifBlank { row.optString("clientName") },
    proposals = row.optJSONArray("proposals")?.let { arr ->
        (0 until arr.length()).map { parseProposal(arr.getJSONObject(it)) }
    } ?: emptyList()
)

fun parseFavorite(row: JSONObject): Favorite = Favorite(
    photographerId = row.getLong("photographerId"),
    slug = row.optString("slug"),
    name = row.optString("name"),
    createdAt = row.optString("createdAt")
)