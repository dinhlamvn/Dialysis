package com.dialysis.app.ui.login

import com.dialysis.app.base.BaseState

data class LoginState(
    val identifier: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoginLoading: Boolean = false,
    val loginError: String? = null,
    val isLoginSuccess: Boolean = false,
) : BaseState
