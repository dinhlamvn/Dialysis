package com.dialysis.app.ui.otpverify

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.VerifyOtpRequest
import com.dialysis.app.domain.auth.RequestPasswordResetOtpUseCase
import com.dialysis.app.router.Router
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sync.WaterIntakeSyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OtpVerifyViewModel(
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager,
    private val waterIntakeSyncScheduler: WaterIntakeSyncScheduler,
    private val requestPasswordResetOtp: RequestPasswordResetOtpUseCase
) : BaseViewModel<OtpVerifyState>(OtpVerifyState()) {

    val identifierTypeState = collectStateUI(OtpVerifyState::identifierType)
    val identifierState = collectStateUI(OtpVerifyState::identifier)
    val modeState = collectStateUI(OtpVerifyState::mode)
    val otpCodeState = collectStateUI(OtpVerifyState::otpCode)
    val isVerifyingState = collectStateUI(OtpVerifyState::isVerifying)
    val isResendingState = collectStateUI(OtpVerifyState::isResending)
    val verifyErrorState = collectStateUI(OtpVerifyState::verifyError)
    val resendMessageState = collectStateUI(OtpVerifyState::resendMessage)

    fun setIdentifierData(identifierType: String, identifier: String, mode: String) = setState {
        copy(identifierType = identifierType, identifier = identifier, mode = mode.ifBlank { Router.OTP_MODE_REGISTRATION })
    }

    fun updateOtpCode(value: String) = setState {
        copy(
            otpCode = value.filter { it.isDigit() }.take(6),
            verifyError = null
        )
    }

    fun getOtpCode(): String {
        var otp = ""
        getState { state -> otp = state.otpCode }
        return otp
    }

    fun verifyOtp() {
        getState { state ->
            if (state.isVerifying) return@getState

            if (state.identifierType.isBlank() || state.identifier.isBlank()) {
                setState { copy(verifyError = "Thiếu thông tin định danh") }
                return@getState
            }

            if (state.otpCode.length != 6) {
                setState { copy(verifyError = "Mã OTP phải gồm 6 chữ số") }
                return@getState
            }

            if (state.mode == Router.OTP_MODE_PASSWORD_RESET) {
                setState { copy(verifyError = null, isVerifySuccess = true) }
                return@getState
            }

            setState {
                copy(
                    isVerifying = true,
                    verifyError = null,
                    isVerifySuccess = false
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                val request = VerifyOtpRequest(
                    identifierType = state.identifierType,
                    identifier = state.identifier,
                    otpCode = state.otpCode,
                )

                val result = networkManager.resolve {
                    networkManager.appPublicServices.verifyOtp(request)
                }

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: return@launch
                    accountSharePref.setToken(data.token)
                    accountSharePref.setTokenType(data.tokenType)
                    waterIntakeSyncScheduler.enqueue()
                    setState {
                        copy(
                            isVerifying = false,
                            verifyError = null,
                            isVerifySuccess = true
                        )
                    }
                } else {
                    setState {
                        copy(
                            isVerifying = false,
                            verifyError = result.exceptionOrNull()?.message,
                            isVerifySuccess = false
                        )
                    }
                }
            }
        }
    }

    fun resendOtp() {
        getState { state ->
            if (state.isResending) return@getState
            if (state.mode != Router.OTP_MODE_PASSWORD_RESET) return@getState
            if (state.identifier.isBlank()) {
                setState { copy(verifyError = "Thiếu thông tin định danh") }
                return@getState
            }

            setState { copy(isResending = true, verifyError = null, resendMessage = null) }
            viewModelScope.launch {
                val result = requestPasswordResetOtp(state.identifier)
                setState {
                    if (result.isSuccess) {
                        copy(isResending = false, resendMessage = "Mã OTP đã được gửi lại.")
                    } else {
                        copy(
                            isResending = false,
                            verifyError = result.exceptionOrNull()?.message ?: "Không thể gửi lại mã OTP"
                        )
                    }
                }
            }
        }
    }
}
