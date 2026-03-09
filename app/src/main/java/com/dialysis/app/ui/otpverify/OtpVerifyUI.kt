package com.dialysis.app.ui.otpverify

import androidx.compose.foundation.background
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
    val verifyError by viewModel.verifyErrorState.collectAsStateWithLifecycle()
    val isVerifying by viewModel.isVerifyingState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.otp_verify_title),
                color = TitleColor,
                style = TextStyles.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

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

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.otp_verify_confirm),
                onClick = viewModel::verifyOtp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isVerifying) {
            Loading()
        }
    }
}
