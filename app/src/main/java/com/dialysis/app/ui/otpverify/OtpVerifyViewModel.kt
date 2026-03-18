package com.dialysis.app.ui.otpverify

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.VerifyOtpRequest
import com.dialysis.app.sharepref.AccountSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OtpVerifyViewModel(
    private val accountSharePref: AccountSharePref
) : BaseViewModel<OtpVerifyState>(OtpVerifyState()) {

    val identifierTypeState = flowOf(OtpVerifyState::identifierType).collectStateUI("")
    val identifierState = flowOf(OtpVerifyState::identifier).collectStateUI("")
    val otpCodeState = flowOf(OtpVerifyState::otpCode).collectStateUI("")
    val isVerifyingState = flowOf(OtpVerifyState::isVerifying).collectStateUI(false)
    val verifyErrorState = flowOf(OtpVerifyState::verifyError).collectStateUI(null)

    fun setIdentifierData(identifierType: String, identifier: String) = setState {
        copy(identifierType = identifierType, identifier = identifier)
    }

    fun updateOtpCode(value: String) = setState {
        copy(
            otpCode = value.filter { it.isDigit() }.take(6),
            verifyError = null
        )
    }

    fun verifyOtp() {
        getState { state ->
            if (state.isVerifying) return@getState

            if (state.identifierType.isBlank() || state.identifier.isBlank()) {
                setState { copy(verifyError = "Missing identifier data") }
                return@getState
            }

            if (state.otpCode.length != 6) {
                setState { copy(verifyError = "OTP must be 6 digits") }
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

                val result = NetworkManager.resolve {
                    NetworkManager.appPublicServices.verifyOtp(request)
                }

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: return@launch
                    accountSharePref.setToken(data.token)
                    accountSharePref.setTokenType(data.tokenType)
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
}
