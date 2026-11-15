package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpRequest
import com.lensclick.app.data.remote.auth.HttpTransport
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.data.remote.auth.SessionStore
import com.lensclick.app.data.remote.auth.ensureHttpSuccess
import com.lensclick.app.domain.PortfolioGateway
import com.lensclick.app.domain.PortfolioItem
import java.io.ByteArrayOutputStream
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

class HttpPortfolioGateway(
    private val transport: HttpTransport,
    private val sessionStore: SessionStore
) : PortfolioGateway {
    override suspend fun list(): List<PortfolioItem> {
        val response = transport.execute(bearerRequest("GET", "/api/account/portfolio"))
        ensureHttpSuccess(response, 200)
        return parseList(response.body)
    }

    override suspend fun add(
        image: ByteArray,
        imageContentType: String,
        altText: String,
        caption: String,
        category: String
    ): PortfolioItem {
        val boundary = "----LensClickPortfolio${UUID.randomUUID()}"
        val body = portfolioMultipartBody(image, imageContentType, altText, caption, category, boundary)
        val response = transport.execute(
            bearerRequest(
                "POST",
                "/api/account/portfolio",
                bodyBytes = body,
                contentType = "multipart/form-data; boundary=$boundary"
            )
        )
        ensureHttpSuccess(response, 201)
        return try {
            parsePortfolioItem(JSONObject(response.body).getJSONObject("item"))
        } catch (error: Exception) {
            throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE, cause = error)
        }
    }

    override suspend fun update(items: List<PortfolioItem>): List<PortfolioItem> {
        val payload = JSONObject().put("items", JSONArray().apply {
            items.forEach { put(portfolioItemJson(it)) }
        }).toString()
        val response = transport.execute(bearerRequest("PUT", "/api/account/portfolio", payload))
        ensureHttpSuccess(response, 200)
        return parseList(response.body)
    }

    override suspend fun remove(id: Long) {
        val response = transport.execute(bearerRequest("DELETE", "/api/account/portfolio/$id"))
        ensureHttpSuccess(response, 200)
    }

    private fun parseList(body: String): List<PortfolioItem> = try {
        parsePortfolioList(body)
    } catch (error: Exception) {
        throw MobileAuthException(MobileAuthErrorCode.MALFORMED_RESPONSE, cause = error)
    }

    private fun bearerRequest(
        method: String,
        path: String,
        body: String? = null,
        bodyBytes: ByteArray? = null,
        contentType: String = "application/json"
    ): HttpRequest {
        val token = sessionStore.readAccessToken()
            ?: throw MobileAuthException(MobileAuthErrorCode.UNAUTHENTICATED)
        val headers = buildMap {
            put("Accept", "application/json")
            put("Authorization", "Bearer $token")
            if (body != null || bodyBytes != null) put("Content-Type", contentType)
        }
        return HttpRequest(method, path, headers, body, bodyBytes)
    }
}

internal fun portfolioItemJson(item: PortfolioItem): JSONObject = JSONObject()
    .put("id", item.id)
    .put("caption", item.caption)
    .put("altText", item.altText)
    .put("position", item.position)
    .put("isCover", item.isCover)
    .put("category", item.category)

internal fun portfolioMultipartBody(
    image: ByteArray,
    imageContentType: String,
    altText: String,
    caption: String,
    category: String,
    boundary: String
): ByteArray {
    val out = ByteArrayOutputStream()
    fun field(name: String, value: String) {
        out.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n".toByteArray(Charsets.UTF_8))
    }
    out.write(
        "--$boundary\r\nContent-Disposition: form-data; name=\"image\"; filename=\"portfolio\"\r\nContent-Type: $imageContentType\r\n\r\n"
            .toByteArray(Charsets.UTF_8)
    )
    out.write(image)
    out.write("\r\n".toByteArray(Charsets.UTF_8))
    field("altText", altText)
    field("caption", caption)
    field("category", category)
    out.write("--$boundary--\r\n".toByteArray(Charsets.UTF_8))
    return out.toByteArray()
}

fun parsePortfolioItem(row: JSONObject): PortfolioItem = PortfolioItem(
    id = row.getLong("id"),
    caption = row.optString("caption"),
    altText = row.optString("altText"),
    position = row.optInt("position"),
    isCover = row.optBoolean("isCover"),
    category = row.optString("category", "other"),
    imageUrl = row.optString("imageUrl")
)

fun parsePortfolioList(json: String): List<PortfolioItem> {
    val rows = JSONObject(json).getJSONArray("portfolio")
    return (0 until rows.length()).map { parsePortfolioItem(rows.getJSONObject(it)) }
}