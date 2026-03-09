package com.dialysis.app.ui.otpverify

import com.dialysis.app.base.BaseState

data class OtpVerifyState(
    val identifierType: String = "",
    val identifier: String = "",
    val otpCode: String = "",
    val isVerifying: Boolean = false,
    val verifyError: String? = null,
    val isVerifySuccess: Boolean = false,
) : BaseState
