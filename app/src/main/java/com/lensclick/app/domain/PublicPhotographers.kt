package com.lensclick.app.domain

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

data class PublicPhotographerProfile(
    val profile: PublicPhotographer,
    val bio: String,
    val serviceRadiusKm: Int?,
    val availability: String,
    val instagram: String,
    val website: String,
    val profilePic: String,
    val contacts: PublicPhotographerContacts,
    val portfolio: List<PortfolioItem>,
    val isPublished: Boolean
)

data class PublicPhotographerContacts(val email: String, val phone: String)

interface PublicPhotographersGateway {
    suspend fun list(city: String, specialty: String?, page: Int): PublicPhotographerPage
    /**
     * Fetches the public profile and its embedded portfolio for a slug.
     *
     * The default keeps existing gateway implementations source-compatible
     * while callers migrate to the profile endpoint.
     */
    suspend fun get(slug: String): PublicPhotographerProfile =
        throw UnsupportedOperationException("Public profile lookup is not implemented")
}

class PublicPhotographerNotFoundException(val slug: String) : java.io.IOException("Photographer not found: $slug")