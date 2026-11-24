package com.lensclick.app.domain

data class AccountDataExport(
    val rawJson: String
)

interface AccountGateway {
    suspend fun enableProfessionalAccount(phone: String): MobileUser
    suspend fun changePassword(currentPassword: String, newPassword: String)
    suspend fun exportData(): AccountDataExport
    suspend fun deleteAccount(password: String, confirmation: String = "EXCLUIR"): Boolean
}
