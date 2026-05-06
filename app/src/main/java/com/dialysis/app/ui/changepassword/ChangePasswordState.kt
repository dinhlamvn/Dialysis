package com.dialysis.app.ui.changepassword

import com.dialysis.app.base.BaseState

data class ChangePasswordState(
    val identifier: String = "",
    val otpCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) : BaseState
