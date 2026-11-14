package com.lensclick.app.domain

data class ProfessionalProfile(
    val bio: String = "",
    val specialties: List<String> = emptyList(),
    val city: String = "",
    val state: String = "",
    val serviceRadiusKm: Int? = null,
    val startingPriceCents: Long? = null,
    val availability: String = "available",
    val contacts: ProfessionalContacts = ProfessionalContacts(),
    val showEmail: Boolean = false,
    val showPhone: Boolean = false,
    val isPublished: Boolean = false
)

data class ProfessionalContacts(
    val email: String = "",
    val phone: String = ""
)

// Implementação remota aguarda contrato móvel aprovado; critérios de publicação e autorização são do servidor.
interface ProfessionalProfileGateway {
    suspend fun load(): ProfessionalProfile

    suspend fun save(profile: ProfessionalProfile): ProfessionalProfile
}