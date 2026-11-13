package com.lensclick.app.data.remote

import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.domain.PublicPhotographerContacts
import com.lensclick.app.domain.PublicPhotographerNotFoundException
import com.lensclick.app.domain.PublicPhotographerPage
import com.lensclick.app.domain.PublicPhotographerProfile
import com.lensclick.app.domain.PublicPhotographersGateway
import com.lensclick.app.domain.PublicPortfolioItem
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HttpPublicPhotographersGateway(
    private val baseUrl: String = ApiEnvironment.baseUrl
) : PublicPhotographersGateway {
    override suspend fun list(city: String, specialty: String?, page: Int): PublicPhotographerPage =
        withContext(Dispatchers.IO) {
            val params = buildList {
                add("page=${page.coerceAtLeast(1)}")
                add("pageSize=12")
                if (city.isNotBlank()) add("city=${java.net.URLEncoder.encode(city.trim(), "UTF-8")}")
                if (specialty != null) add("specialty=${java.net.URLEncoder.encode(specialty, "UTF-8")}")
            }.joinToString("&")
            val connection = URL("$baseUrl/api/photographers?$params").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw java.io.IOException("Photographers HTTP ${connection.responseCode}")
                }
                parsePublicPhotographerPage(connection.inputStream.bufferedReader().use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }

    override suspend fun get(slug: String): PublicPhotographerProfile =
        withContext(Dispatchers.IO) {
            require(slug.matches(Regex("[a-z0-9-]{3,80}"))) { "Invalid photographer slug" }
            val connection = URL("$baseUrl/api/photographers/${java.net.URLEncoder.encode(slug, "UTF-8")}")
                .openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw PublicPhotographerNotFoundException(slug)
                }
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw java.io.IOException("Photographer HTTP ${connection.responseCode}")
                }
                parsePublicPhotographerProfile(connection.inputStream.bufferedReader().use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }
}

fun parsePublicPhotographerPage(json: String): PublicPhotographerPage {
    val root = JSONObject(json)
    val rows = root.getJSONArray("photographers")
    val pagination = root.getJSONObject("pagination")
    val photographers = (0 until rows.length()).map { index ->
        val row = rows.getJSONObject(index)
        val specialties = row.optJSONArray("specialties")
        PublicPhotographer(
            slug = row.getString("slug"),
            name = row.getString("name"),
            city = row.optString("city"),
            state = row.optString("state"),
            specialties = if (specialties == null) emptyList() else (0 until specialties.length()).map { specialties.getString(it) },
            startingPriceCents = if (row.isNull("startingPriceCents")) null else row.getLong("startingPriceCents"),
            ratingAverage = if (row.isNull("ratingAverage")) null else row.getDouble("ratingAverage"),
            reviewCount = row.optInt("reviewCount", 0)
        )
    }

    return PublicPhotographerPage(
        photographers = photographers,
        page = pagination.getInt("page"),
        total = pagination.getInt("total"),
        totalPages = pagination.getInt("totalPages")
    )
}

fun parsePublicPhotographerProfile(json: String): PublicPhotographerProfile {
    val root = JSONObject(json).getJSONObject("profile")
    val portfolio = root.optJSONArray("portfolio")
    val photographer = PublicPhotographer(
        slug = root.getString("slug"),
        name = root.getString("name"),
        city = root.optString("city"),
        state = root.optString("state"),
        specialties = root.optJSONArray("specialties")?.let { values ->
            (0 until values.length()).map(values::getString)
        } ?: emptyList(),
        startingPriceCents = if (root.isNull("startingPriceCents")) null else root.getLong("startingPriceCents"),
        ratingAverage = if (root.isNull("ratingAverage")) null else root.getDouble("ratingAverage"),
        reviewCount = root.optInt("reviewCount", 0)
    )
    val contacts = root.optJSONObject("contacts")
    return PublicPhotographerProfile(
        profile = photographer,
        bio = root.optString("bio"),
        serviceRadiusKm = if (root.isNull("serviceRadiusKm")) null else root.optInt("serviceRadiusKm"),
        availability = root.optString("availability", "available"),
        instagram = root.optString("instagram"),
        website = root.optString("website"),
        profilePic = root.optString("profilePic"),
        contacts = PublicPhotographerContacts(
            email = contacts?.optString("email").orEmpty(),
            phone = contacts?.optString("phone").orEmpty()
        ),
        portfolio = if (portfolio == null) emptyList() else (0 until portfolio.length()).map { index ->
            val item = portfolio.getJSONObject(index)
            PublicPortfolioItem(
                id = item.getLong("id"),
                caption = item.optString("caption"),
                altText = item.optString("altText"),
                position = item.optInt("position"),
                isCover = item.optBoolean("isCover"),
                category = item.optString("category", "other"),
                imageUrl = item.optString("imageUrl")
            )
        },
        isPublished = root.optBoolean("isPublished", true)
    )
}