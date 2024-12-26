package ai.create.photo.ui.settings.login

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.email
import photocreateai.composeapp.generated.resources.invalid_email
import photocreateai.composeapp.generated.resources.send_otp

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
            Screen(
                email = state.email,
                onEmailChanged = viewModel::onEmailChanged,
                sendOtp = viewModel::sendOtp,
                isInvalidEmail = state.isInvalidEmail,
            )
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }
    }
}

@Composable
private fun Screen(
    email: String,
    onEmailChanged: (String) -> Unit,
    sendOtp: () -> Unit,
    isInvalidEmail: Boolean,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .padding(horizontal = 24.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextField(email, onEmailChanged, sendOtp, isInvalidEmail)
        SendOtpButton(sendOtp)
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
        label = { Text(stringResource(Res.string.email)) },
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
fun SendOtpButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
    ) {
        Text(
            text = stringResource(Res.string.send_otp),
            fontSize = 18.sp,
        )
    }
}


