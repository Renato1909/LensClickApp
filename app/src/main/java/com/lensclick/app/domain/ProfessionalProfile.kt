package com.lensclick.app.domain

data class ProfessionalProfile(
    val slug: String = "",
    val bio: String = "",
    val specialties: List<String> = emptyList(),
    val city: String = "",
    val state: String = "",
    val serviceRadiusKm: Int? = null,
    val startingPriceCents: Long? = null,
    val availability: String = "available",
    val ratingAverage: Double? = null,
    val reviewCount: Int = 0,
    val instagram: String = "",
    val website: String = "",
    val publicEmail: String = "",
    val publicPhone: String = "",
    val showEmail: Boolean = false,
    val showPhone: Boolean = false,
    val isPublished: Boolean = false
)

// Consome /api/account/professional-profile com Bearer; critérios de publicação e autorização são do servidor.
interface ProfessionalProfileGateway {
    suspend fun load(): ProfessionalProfile

    suspend fun save(profile: ProfessionalProfile): ProfessionalProfile
}