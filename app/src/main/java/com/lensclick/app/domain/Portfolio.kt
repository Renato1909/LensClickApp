package com.lensclick.app.domain

data class PortfolioItem(
    val id: Long,
    val caption: String,
    val altText: String,
    val position: Int,
    val isCover: Boolean,
    val category: String,
    val imageUrl: String
)

// Limites do contrato web documentado; a validação autoritativa pertence ao servidor.
object PortfolioLimits {
    const val MAX_ITEMS = 12
    const val MAX_IMAGE_BYTES = 4_000_000
    const val MAX_COVERS = 1
}

// Implementação remota aguarda contrato móvel aprovado; limites e autorização são do servidor.
interface PortfolioGateway {
    suspend fun list(): List<PortfolioItem>

    suspend fun add(image: ByteArray, altText: String, caption: String, category: String): PortfolioItem

    suspend fun update(items: List<PortfolioItem>): List<PortfolioItem>

    suspend fun remove(id: Long)
}