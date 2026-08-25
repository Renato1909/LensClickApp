package br.com.lensclick.app.data.remote

import br.com.lensclick.app.data.model.QuotesResponse
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Repositório das solicitações de orçamento (quote requests). */
class QuotesRepository {

    /** Cliente solicita orçamento para um fotógrafo (por slug). */
    suspend fun requestQuote(
        slug: String,
        eventType: String,
        eventDate: String,
        city: String,
        budgetCents: Long?,
        message: String
    ): MessageResponse {
        val body = buildJsonObject {
            put("slug", slug)
            put("eventType", eventType)
            put("eventDate", eventDate)
            put("city", city)
            budgetCents?.let { put("budgetCents", it) }
            put("message", message)
        }
        return ApiClient.request {
            method = HttpMethod.Post
            url("/api/quotes")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Lista os orçamentos do usuário autenticado.
     * @param box "sent" (cliente) ou "received" (fotógrafo).
     */
    suspend fun listQuotes(box: String, page: Int = 1): QuotesResponse =
        ApiClient.get("/api/quotes?box=$box&page=$page")

    /** Fotógrafo aceita ou recusa um orçamento pendente. */
    suspend fun respondQuote(quoteId: Long, status: String): MessageResponse =
        ApiClient.request {
            method = HttpMethod.Put
            url("/api/quotes/$quoteId")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("status", status) })
        }
}
