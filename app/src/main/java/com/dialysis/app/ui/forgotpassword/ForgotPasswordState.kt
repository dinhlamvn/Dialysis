package com.dialysis.app.ui.forgotpassword

import com.dialysis.app.base.BaseState

data class ForgotPasswordState(
    val identifier: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successIdentifier: String = "",
    val successIdentifierType: String = "",
    val isOtpSent: Boolean = false
) : BaseState
