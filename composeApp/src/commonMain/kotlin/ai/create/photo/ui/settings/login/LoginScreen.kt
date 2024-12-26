package ai.create.photo.ui.settings.login

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.enter_code
import photocreateai.composeapp.generated.resources.enter_email
import photocreateai.composeapp.generated.resources.invalid_email
import photocreateai.composeapp.generated.resources.send_otp
import photocreateai.composeapp.generated.resources.verify
import photocreateai.composeapp.generated.resources.wrong_code

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel { LoginViewModel() },
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val state = viewModel.uiState

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else {
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(state = rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val sendOtp: () -> Unit = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.sendOtp()
                }
                EmailTextField(
                    email = state.email,
                    onEmailChanged = viewModel::onEmailChanged,
                    sendOtp = sendOtp,
                    isInvalidEmail = state.isInvalidEmail
                )
                SendOtpButton(state.isSendingOtp, sendOtp)
                Spacer(modifier = Modifier.height(16.dp))
                EnterCode(
                    enterOtp = state.enterOtp,
                    otp = state.otp,
                    isIncorrectOtp = state.isIncorrectOtp,
                    onOtpChanged = viewModel::onOtpChanged
                )
                ConfirmOtpButton(state.enterOtp, state.isVerifyingOtp, viewModel::verifyOtp)
            }
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }
    }
}

@Composable
private fun EmailTextField(
    email: String,
    onEmailChanged: (String) -> Unit,
    sendOtp: () -> Unit,
    isInvalidEmail: Boolean,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = onEmailChanged,
        label = { Text(stringResource(Res.string.enter_email)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { sendOtp() }),
        singleLine = true,
        isError = isInvalidEmail,
        supportingText = {
            if (isInvalidEmail) {
                Text(stringResource(Res.string.invalid_email))
            }
        }
    )
}

@Composable
fun SendOtpButton(isLoading: Boolean, onClick: () -> Unit) {
    if (isLoading) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onSurface,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
        )
    } else {
        TextButton(
            onClick = onClick,
        ) {
            Text(
                text = stringResource(Res.string.send_otp),
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
fun EnterCode(
    enterOtp: Boolean,
    otp: String,
    isIncorrectOtp: Boolean,
    onOtpChanged: (String) -> Unit
) {
    Crossfade(
        targetState = enterOtp,
        label = stringResource(Res.string.enter_code),
    ) { showEnterCode ->
        Spacer(modifier = Modifier.height(16.dp))
        val focusRequester = remember { FocusRequester() }
        if (showEnterCode) {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester).fillMaxWidth(),
                value = otp,
                onValueChange = onOtpChanged,
                label = { Text(stringResource(Res.string.enter_code)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isIncorrectOtp,
                supportingText = {
                    if (isIncorrectOtp) {
                        Text(stringResource(Res.string.wrong_code))
                    }
                }
            )
        }
        LaunchedEffect(showEnterCode) {
            if (showEnterCode) focusRequester.requestFocus()
        }
    }
}

@Composable
fun ConfirmOtpButton(showButton: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Crossfade(
        targetState = showButton,
        label = stringResource(Res.string.verify),
    ) { showButton ->
        if (showButton) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                TextButton(
                    onClick = onClick,
                ) {
                    Text(
                        text = stringResource(Res.string.verify),
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}


