package com.dialysis.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
private val InputCardBackground = Color(0xFFFFFFFF)
private val InputShape = RoundedCornerShape(32.dp)
private val InputTextStyle: TextStyle = TextStyles.body

@Composable
fun RegisterScreen(viewModel: RegisterViewModel = viewModel()) {
    val emailOrPhone by viewModel.emailOrPhoneState.collectAsStateWithLifecycle()
    val name by viewModel.nameState.collectAsStateWithLifecycle()
    val password by viewModel.passwordState.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPasswordState.collectAsStateWithLifecycle()
    val registerError by viewModel.registerErrorState.collectAsStateWithLifecycle()
    val isRegistering by viewModel.isRegisterLoadingState.collectAsStateWithLifecycle()


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.register_auth_title),
                color = TitleColor,
                style = TextStyles.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputCardField(
                value = emailOrPhone,
                onValueChange = viewModel::updateEmailOrPhone,
                label = stringResource(R.string.register_field_email_or_phone),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = InputTextStyle,
                labelTextStyle = TextStyles.body,
                shape = InputShape,
                containerColor = InputCardBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputCardField(
                value = name,
                onValueChange = viewModel::updateName,
                label = stringResource(R.string.register_name_title),
                textStyle = InputTextStyle,
                labelTextStyle = TextStyles.body,
                shape = InputShape,
                containerColor = InputCardBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputCardField(
                value = password,
                onValueChange = viewModel::updatePassword,
                label = stringResource(R.string.register_field_password),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = InputTextStyle,
                labelTextStyle = TextStyles.body,
                shape = InputShape,
                containerColor = InputCardBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputCardField(
                value = confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label = stringResource(R.string.register_field_confirm_password),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = InputTextStyle,
                labelTextStyle = TextStyles.body,
                shape = InputShape,
                containerColor = InputCardBackground
            )

            if (!registerError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = registerError.orEmpty(),
                    color = Color.Red,
                    style = TextStyles.body,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.register_create_account),
                onClick = viewModel::register,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isRegistering) {
            Loading()
        }
    }
}
