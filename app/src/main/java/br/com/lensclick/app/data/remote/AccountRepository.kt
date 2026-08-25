package br.com.lensclick.app.data.remote

import br.com.lensclick.app.data.model.User
import br.com.lensclick.app.data.model.UserResponse
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProfessionalProfile(
    val slug: String = "",
    val bio: String = "",
    val specialties: List<String> = emptyList(),
    val city: String = "",
    val state: String = "",
    val serviceRadiusKm: Double? = null,
    val startingPriceCents: Long? = null,
    val availability: String = "available",
    val publicEmail: String = "",
    val publicPhone: String = "",
    val instagram: String = "",
    val website: String = "",
    val showEmail: Boolean = false,
    val showPhone: Boolean = false,
    val isPublished: Boolean = false
)

@Serializable
data class ProfessionalProfileResponse(val profile: ProfessionalProfile?)

@Serializable
data class MessageResponse(val message: String = "")

class AccountRepository {

    suspend fun me(): User = ApiClient.get<UserResponse>("/api/auth/me").user

    /** Atualiza dados pessoais. Se o e-mail mudar, o backend exige currentPassword. */
    suspend fun updateProfile(
        name: String,
        email: String,
        phone: String,
        role: String,
        gender: String,
        age: Long?,
        profilePic: String,
        currentPassword: String? = null
    ): User {
        val body = buildJsonObject {
            put("name", name)
            put("email", email)
            put("phone", phone)
            put("role", role)
            put("gender", gender)
            age?.let { put("age", it) }
            put("profilePic", profilePic)
            if (!currentPassword.isNullOrBlank()) put("currentPassword", currentPassword)
        }
        return ApiClient.request {
            method = HttpMethod.Put
            url("/api/profile")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): MessageResponse =
        ApiClient.request {
            method = HttpMethod.Put
            url("/api/account/password")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("currentPassword", currentPassword)
                put("newPassword", newPassword)
            })
        }

    suspend fun getProfessionalProfile(): ProfessionalProfile {
        val response = ApiClient.get<ProfessionalProfileResponse>("/api/account/professional-profile")
        return response.profile ?: ProfessionalProfile()
    }

    suspend fun updateProfessionalProfile(profile: ProfessionalProfile): ProfessionalProfile {
        val body = buildJsonObject {
            put("bio", profile.bio)
            put("specialties", kotlinx.serialization.json.JsonArray(profile.specialties.map { kotlinx.serialization.json.JsonPrimitive(it) }))
            put("city", profile.city)
            put("state", profile.state)
            profile.serviceRadiusKm?.let { put("serviceRadiusKm", it) }
            profile.startingPriceCents?.let { put("startingPriceCents", it) }
            put("availability", profile.availability)
            put("publicEmail", profile.publicEmail)
            put("publicPhone", profile.publicPhone)
            put("instagram", profile.instagram)
            put("website", profile.website)
            put("showEmail", profile.showEmail)
            put("showPhone", profile.showPhone)
            put("isPublished", profile.isPublished)
        }
        return ApiClient.request {
            method = HttpMethod.Put
            url("/api/account/professional-profile")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Atualiza a foto de perfil. O backend recebe a imagem via PUT /api/profile,
     * no campo `profilePic`, como data URL base64 (png/jpeg/webp, máx 450 KB).
     * Mantém a foto atual quando o dataUrl é o caminho /api/profile-picture/{id}.
     */
    suspend fun updateProfilePicture(currentProfile: User, imageBytes: ByteArray?, mimeType: String): User {
        val profilePic = when {
            imageBytes == null -> "" // remove a foto atual
            else -> {
                val ext = when (mimeType) {
                    "image/jpeg" -> "jpeg"
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> throw ApiException("Formato de imagem não permitido.", 400)
                }
                if (imageBytes.size > 450_000) throw ApiException("A imagem deve ter no máximo 450 KB.", 413)
                "data:image/$ext;base64," + android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            }
        }
        return updateProfile(
            name = currentProfile.name,
            email = currentProfile.email,
            phone = currentProfile.phone,
            role = currentProfile.role,
            gender = currentProfile.gender ?: "other",
            age = currentProfile.age,
            profilePic = profilePic
        )
    }

    /** Exclui a conta permanentemente. Backend: DELETE /api/account com confirmation EXCLUIR + senha atual. */
    suspend fun deleteAccount(password: String): MessageResponse =
        ApiClient.request {
            method = HttpMethod.Delete
            url("/api/account")
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("confirmation", "EXCLUIR")
                put("currentPassword", password)
            })
        }
}
