package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.Engagement
import com.lensclick.app.domain.EngagementAction
import com.lensclick.app.domain.EngagementDetail
import com.lensclick.app.domain.EngagementDraft
import com.lensclick.app.domain.EngagementEvent
import com.lensclick.app.domain.EngagementGateway
import com.lensclick.app.domain.PaymentMethod
import org.json.JSONObject

class HttpEngagementGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : EngagementGateway {
    override suspend fun create(
        slug: String,
        reference: String,
        priceCents: Long,
        proposalId: Long?,
        draft: EngagementDraft
    ): Engagement {
        require(slug.matches(Regex("[a-z0-9-]{3,80}")))
        val body = JSONObject()
            .put("reference", reference)
            .put("priceCents", priceCents)
            .put("proposalId", proposalId ?: JSONObject.NULL)
            .put("category", draft.category)
            .put("description", draft.description)
            .put("eventType", draft.eventType)
            .put("eventDate", draft.eventDate)
            .put("start", draft.start)
            .put("end", draft.end)
            .put("venue", draft.venue)
            .put("address", draft.address)
            .put("city", draft.city)
            .put("state", draft.state)
            .put("venueDetails", draft.venueDetails)
            .put("notes", draft.notes)
            .put("paymentMethod", draft.paymentMethod.wire)
        return request("POST", "/api/photographers/$slug/engagements", body, 200, 201)
            .getJSONObject("engagement").toEngagement()
    }

    override suspend fun list(): List<Engagement> {
        val rows = request("GET", "/api/account/engagements", null, 200).getJSONArray("engagements")
        return List(rows.length()) { rows.getJSONObject(it).toEngagement() }
    }

    override suspend fun detail(id: Long): EngagementDetail {
        val json = request("GET", "/api/account/engagements/$id", null, 200)
        val rows = json.getJSONArray("history")
        return EngagementDetail(json.getJSONObject("engagement").toEngagement(), List(rows.length()) {
            val row = rows.getJSONObject(it)
            EngagementEvent(row.getString("status"), row.getString("paymentStatus"), row.getString("createdAt"))
        })
    }

    override suspend fun update(id: Long, action: EngagementAction): Engagement =
        request("PUT", "/api/account/engagements/$id", JSONObject().put("action", action.wire), 200)
            .getJSONObject("engagement").toEngagement()

    override suspend fun review(id: Long, rating: Int, comment: String) {
        val payload = JSONObject().put("rating", rating).put("comment", comment)
        request("POST", "/api/account/engagements/$id/review", payload, 201)
    }

    private suspend fun request(method: String, path: String, body: JSONObject?, vararg statuses: Int): JSONObject {
        val response = transport.execute(sessionStore.bearerRequest(method, path, body?.toString()))
        ensureHttpSuccess(response, *statuses)
        return JSONObject(response.body)
    }
}

private fun JSONObject.toEngagement() = Engagement(
    id = getLong("id"), slug = optString("slug"), clientName = getString("clientName"),
    photographerName = getString("photographerName"),
    service = EngagementDraft(
        category = getString("category"), description = getString("description"), eventType = getString("eventType"),
        eventDate = getString("eventDate"), start = getString("start"), end = getString("end"), venue = getString("venue"),
        address = getString("address"), city = getString("city"), state = getString("state"), venueDetails = optString("venueDetails"),
        notes = optString("notes"), paymentMethod = PaymentMethod.entries.first { it.wire == getString("paymentMethod") }
    ),
    priceCents = getLong("priceCents"), feeCents = getLong("feeCents"), paymentStatus = getString("paymentStatus"),
    status = getString("status"), reviewPublished = optBoolean("reviewPublished"), createdAt = getString("createdAt")
)
