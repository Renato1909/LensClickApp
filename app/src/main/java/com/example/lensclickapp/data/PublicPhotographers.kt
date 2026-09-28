package com.example.lensclickapp.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class PublicPhotographer(
    val slug: String,
    val name: String,
    val city: String,
    val state: String,
    val specialties: List<String>,
    val startingPriceCents: Long?,
    val ratingAverage: Double?,
    val reviewCount: Int
)

data class PublicPhotographerPage(
    val photographers: List<PublicPhotographer>,
    val page: Int,
    val total: Int,
    val totalPages: Int
)

interface PublicPhotographersGateway {
    suspend fun list(city: String, specialty: String?, page: Int): PublicPhotographerPage
}

class HttpPublicPhotographersGateway(
    private val baseUrl: String = "https://lensclick.renato-aparecido-business.workers.dev"
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
