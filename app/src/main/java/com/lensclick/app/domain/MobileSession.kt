package com.lensclick.app.domain

data class MobileUser(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val emailVerified: Boolean,
    val isAdmin: Boolean
)

data class MobileSession(
    val user: MobileUser,
    val accessToken: String,
    val expiresAt: String
)