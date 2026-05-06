package com.dialysis.app.ui.otpverify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dialysis.app.R
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.InputCardField
import com.dialysis.app.ui.components.Loading
import com.dialysis.app.ui.components.PrimaryButton
import com.dialysis.app.ui.components.TextStyles

private val PageBackground = Color(0xFFFFFFFF)
private val TitleColor = Color(0xFF111111)
private val InputTextStyle: TextStyle = TextStyles.body

@Composable
fun OtpVerifyScreen(viewModel: OtpVerifyViewModel = viewModel()) {
    val otpCode by viewModel.otpCodeState.collectAsStateWithLifecycle()
    val identifier by viewModel.identifierState.collectAsStateWithLifecycle()
    val mode by viewModel.modeState.collectAsStateWithLifecycle()
    val verifyError by viewModel.verifyErrorState.collectAsStateWithLifecycle()
    val resendMessage by viewModel.resendMessageState.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifyingState.collectAsStateWithLifecycle()
    val isResending by viewModel.isResendingState.collectAsStateWithLifecycle()
    val isPasswordReset = mode == Router.OTP_MODE_PASSWORD_RESET

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (isPasswordReset) {
                    stringResource(R.string.reset_password_otp_title)
                } else {
                    stringResource(R.string.otp_verify_title)
                },
                color = TitleColor,
                style = TextStyles.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (identifier.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.otp_verify_subtitle, identifier),
                    color = Color(0xFF8E8E93),
                    style = TextStyles.body,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            InputCardField(
                value = otpCode,
                onValueChange = viewModel::updateOtpCode,
                label = stringResource(R.string.otp_verify_field_code),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = InputTextStyle,
                labelTextStyle = TextStyles.body,
                modifier = Modifier.fillMaxWidth()
            )

            if (!verifyError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = verifyError.orEmpty(),
                    color = Color.Red,
                    style = TextStyles.body,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!resendMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = resendMessage.orEmpty(),
                    color = Color(0xFF1877F2),
                    style = TextStyles.body,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isPasswordReset) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isResending) {
                        stringResource(R.string.otp_verify_resending)
                    } else {
                        stringResource(R.string.otp_verify_resend)
                    },
                    color = Color(0xFF1877F2),
                    style = TextStyles.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable(enabled = !isResending) {
                        viewModel.resendOtp()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = if (isPasswordReset) {
                    stringResource(R.string.reset_password_continue)
                } else {
                    stringResource(R.string.otp_verify_confirm)
                },
                onClick = viewModel::verifyOtp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isVerifying || isResending) {
            Loading()
        }
    }
}
