package com.dialysis.app.domain.auth

data class PasswordResetOtp(
    val identifier: String,
    val identifierType: String,
    val expiresIn: Int?
)
