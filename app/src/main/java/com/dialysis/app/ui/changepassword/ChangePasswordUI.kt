package com.dialysis.app.ui.changepassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dialysis.app.R
import com.dialysis.app.ui.components.InputCardField
import com.dialysis.app.ui.components.Loading
import com.dialysis.app.ui.components.PrimaryButton
import com.dialysis.app.ui.components.TextStyles

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val newPassword by viewModel.newPasswordState.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPasswordState.collectAsStateWithLifecycle()
    val isPasswordVisible by viewModel.isPasswordVisibleState.collectAsStateWithLifecycle()
    val isConfirmVisible by viewModel.isConfirmPasswordVisibleState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessageState.collectAsStateWithLifecycle()
    val canSubmit = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword == confirmPassword

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.common_back),
                    modifier = Modifier.size(24.dp).clickable(onClick = onBackClick)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.reset_password_title),
                color = Color(0xFF111111),
                style = TextStyles.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.reset_password_subtitle),
                color = Color(0xFF8E8E93),
                style = TextStyles.body,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            PasswordField(newPassword, viewModel::updateNewPassword, isPasswordVisible, viewModel::togglePasswordVisibility, R.string.register_field_password)
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(confirmPassword, viewModel::updateConfirmPassword, isConfirmVisible, viewModel::toggleConfirmPasswordVisibility, R.string.register_field_confirm_password)
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMessage.orEmpty(), color = Color.Red, style = TextStyles.body)
            }
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(R.string.reset_password_change_button),
                onClick = viewModel::changePassword,
                enabled = canSubmit && !isLoading
            )
        }

        if (isLoading) Loading()
    }
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, visible: Boolean, onToggle: () -> Unit, labelRes: Int) {
    InputCardField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(labelRes),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingContent = {
            IconButton(onClick = onToggle) {
                Icon(
                    painter = painterResource(if (visible) R.drawable.ic_eye_off else R.drawable.ic_eye),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
    )
}
