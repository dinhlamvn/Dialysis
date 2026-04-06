package com.dialysis.app.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

private val TitleColor = Color(0xFF111111)
private val AccentBlue = Color(0xFF1877F2)
private val BodyGray = Color(0xFF8E8E93)
private val InputCardBackground = Color(0xFFFFFFFF)
private val InputShape = RoundedCornerShape(32.dp)
private val InputTextStyle: TextStyle = TextStyles.body

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(), onBackClick: () -> Unit) {
    val context = LocalContext.current
    val identifier by viewModel.identifierState.collectAsStateWithLifecycle()
    val password by viewModel.passwordState.collectAsStateWithLifecycle()
    val isPasswordVisible by viewModel.isPasswordVisibleState.collectAsStateWithLifecycle()
    val isLoginLoading by viewModel.isLoginLoadingState.collectAsStateWithLifecycle()
    val loginError by viewModel.loginErrorState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onBackClick)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.intro_text_login),
                color = TitleColor,
                style = TextStyles.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputCardField(
                value = identifier,
                onValueChange = viewModel::updateIdentifier,
                label = stringResource(R.string.register_field_email_or_phone),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                textStyle = InputTextStyle,
                labelTextStyle = TextStyles.body,
                shape = InputShape,
                containerColor = InputCardBackground,
                trailingContent = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) {
                                    R.drawable.ic_eye_off
                                } else {
                                    R.drawable.ic_eye
                                }
                            ),
                            contentDescription = if (isPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            tint = Color.Unspecified
                        )
                    }
                }
            )

            if (!loginError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = loginError.orEmpty(),
                    color = Color.Red,
                    style = TextStyles.body,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(R.string.intro_text_login),
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = BodyGray,
                    style = TextStyles.body
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.login_register_action),
                    color = AccentBlue,
                    style = TextStyles.bodyMedium,
                    modifier = Modifier.clickable {
                        context.startActivity(Router.register(context))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isLoginLoading) {
            Loading()
        }
    }
}
