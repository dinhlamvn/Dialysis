package com.dialysis.app.domain.auth

object AuthInputValidator {
    fun identifierError(identifier: String): String? {
        val trimmed = identifier.trim()
        if (trimmed.isBlank()) return "Vui lòng nhập email hoặc số điện thoại"
        val isEmail = EMAIL_REGEX.matches(trimmed)
        val isPhone = PHONE_REGEX.matches(trimmed)
        return if (isEmail || isPhone) null else "Email hoặc số điện thoại không hợp lệ"
    }

    fun passwordError(password: String): String? {
        if (password.isBlank()) return "Mật khẩu không được để trống"
        return if (password.length < 6) "Mật khẩu phải có ít nhất 6 ký tự" else null
    }

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val PHONE_REGEX = Regex("^\\+?[0-9]{9,15}$")
}
