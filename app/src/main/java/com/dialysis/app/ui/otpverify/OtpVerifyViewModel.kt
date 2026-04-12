package com.dialysis.app.ui.otpverify

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.VerifyOtpRequest
import com.dialysis.app.sharepref.AccountSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OtpVerifyViewModel(
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<OtpVerifyState>(OtpVerifyState()) {

    val identifierTypeState = collectStateUI(OtpVerifyState::identifierType)
    val identifierState = collectStateUI(OtpVerifyState::identifier)
    val otpCodeState = collectStateUI(OtpVerifyState::otpCode)
    val isVerifyingState = collectStateUI(OtpVerifyState::isVerifying)
    val verifyErrorState = collectStateUI(OtpVerifyState::verifyError)

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
                setState { copy(verifyError = "Thiếu thông tin định danh") }
                return@getState
            }

            if (state.otpCode.length != 6) {
                setState { copy(verifyError = "Mã OTP phải gồm 6 chữ số") }
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
