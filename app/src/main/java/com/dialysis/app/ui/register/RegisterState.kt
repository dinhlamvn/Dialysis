package com.dialysis.app.ui.register

import com.dialysis.app.base.BaseState

data class RegisterState(
    val emailOrPhone: String = "",
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isRegisterLoading: Boolean = false,
    val registerError: String? = null,
    val isRegisterSuccess: Boolean = false,
) : BaseState
