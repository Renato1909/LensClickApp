package com.lensclick.app.domain

data class AccountDataExport(
    val rawJson: String
)

interface AccountGateway {
    suspend fun exportData(): AccountDataExport
    suspend fun deleteAccount(password: String, confirmation: String = "EXCLUIR"): Boolean
}
