package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.ProfessionalProfile
import com.lensclick.app.domain.ProfessionalProfileGateway
import org.json.JSONArray
import org.json.JSONObject

class HttpProfessionalProfileGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : ProfessionalProfileGateway {
    override suspend fun load(): ProfessionalProfile {
        val response = transport.execute(bearerRequest("GET", "/api/account/professional-profile"))
        ensureHttpSuccess(response, 200)
        return parseProfile(response.body)
    }

    override suspend fun save(profile: ProfessionalProfile): ProfessionalProfile {
        val response = transport.execute(
            bearerRequest("PUT", "/api/account/professional-profile", professionalProfileBody(profile))
        )
        ensureHttpSuccess(response, 200)
        return parseProfile(response.body)
    }

    private fun parseProfile(body: String): ProfessionalProfile = try {
        parseProfessionalProfile(body)
    } catch (error: Exception) {
        throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE, cause = error)
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

internal fun professionalProfileBody(profile: ProfessionalProfile): String = JSONObject()
    .put("bio", profile.bio)
    .put("specialties", JSONArray(profile.specialties))
    .put("city", profile.city)
    .put("state", profile.state)
    .put("serviceRadiusKm", profile.serviceRadiusKm ?: JSONObject.NULL)
    .put("startingPriceCents", profile.startingPriceCents ?: JSONObject.NULL)
    .put("availability", profile.availability)
    .put("publicEmail", profile.publicEmail)
    .put("publicPhone", profile.publicPhone)
    .put("instagram", profile.instagram)
    .put("website", profile.website)
    .put("showEmail", profile.showEmail)
    .put("showPhone", profile.showPhone)
    .put("isPublished", profile.isPublished)
    .toString()

fun parseProfessionalProfile(json: String): ProfessionalProfile {
    val root = JSONObject(json).getJSONObject("profile")
    return ProfessionalProfile(
        slug = root.optString("slug"),
        bio = root.optString("bio"),
        specialties = root.optJSONArray("specialties")?.let { values ->
            (0 until values.length()).map(values::getString)
        } ?: emptyList(),
        city = root.optString("city"),
        state = root.optString("state"),
        serviceRadiusKm = if (root.isNull("serviceRadiusKm")) null else root.optInt("serviceRadiusKm"),
        startingPriceCents = if (root.isNull("startingPriceCents")) null else root.optLong("startingPriceCents"),
        availability = root.optString("availability", "available"),
        ratingAverage = if (root.isNull("ratingAverage")) null else root.optDouble("ratingAverage"),
        reviewCount = root.optInt("reviewCount", 0),
        instagram = root.optString("instagram"),
        website = root.optString("website"),
        publicEmail = root.optString("publicEmail"),
        publicPhone = root.optString("publicPhone"),
        showEmail = root.optBoolean("showEmail", false),
        showPhone = root.optBoolean("showPhone", false),
        isPublished = root.optBoolean("isPublished", false)
    )
}