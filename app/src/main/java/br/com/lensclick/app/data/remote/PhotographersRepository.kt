package br.com.lensclick.app.data.remote

import br.com.lensclick.app.data.model.PhotographerDetailResponse
import br.com.lensclick.app.data.model.SearchResponse

/**
 * Acesso às rotas públicas de busca e perfil de fotógrafos.
 */
class PhotographersRepository {

    suspend fun search(
        city: String? = null,
        state: String? = null,
        specialty: String? = null,
        availability: String? = null,
        page: Int = 1,
        pageSize: Int = 12
    ): SearchResponse {
        val params = buildList {
            city?.takeIf { it.isNotBlank() }?.let { add("city" to it.trim()) }
            state?.takeIf { it.isNotBlank() }?.let { add("state" to it.trim()) }
            specialty?.takeIf { it.isNotBlank() }?.let { add("specialty" to it) }
            availability?.takeIf { it.isNotBlank() }?.let { add("availability" to it) }
            add("page" to page.toString())
            add("pageSize" to pageSize.toString())
        }
        val query = params.joinToString("&") { "${it.first}=${it.second}" }
        return ApiClient.get("/api/photographers?$query")
    }

    suspend fun detail(slug: String): PhotographerDetailResponse =
        ApiClient.get("/api/photographers/$slug")
}
