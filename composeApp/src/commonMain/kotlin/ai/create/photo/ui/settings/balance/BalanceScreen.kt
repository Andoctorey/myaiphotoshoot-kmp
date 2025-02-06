package ai.create.photo.ui.settings.balance

import ai.create.photo.platform.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.apply
import photocreateai.composeapp.generated.resources.enter_promo_code
import photocreateai.composeapp.generated.resources.wrong_code

@Composable
fun BalanceScreen(
    viewModel: BalanceViewModel = viewModel { BalanceViewModel() },
    onBackClick: () -> Unit,
) {

    BackHandler {
        onBackClick()
    }

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
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(state = rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EnterPromoCode(
                    promoCode = state.promoCode,
                    isIncorrectCode = state.isIncorrectPromoCode,
                    onCodeChanged = viewModel::onPromoCodeChanged
                )
                ApplyPromoCodeButton(
                    isLoading = state.isApplyingPromoCode,
                    onClick = viewModel::applyPromoCode
                )
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
fun EnterPromoCode(
    promoCode: String,
    isIncorrectCode: Boolean,
    onCodeChanged: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = promoCode,
        onValueChange = onCodeChanged,
        label = { Text(stringResource(Res.string.enter_promo_code)) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text
        ),
        singleLine = true,
        isError = isIncorrectCode,
        supportingText = {
            if (isIncorrectCode) {
                Text(stringResource(Res.string.wrong_code))
            }
        }
    )
}

@Composable
fun ApplyPromoCodeButton(isLoading: Boolean, onClick: () -> Unit) {
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
                text = stringResource(Res.string.apply),
                fontSize = 18.sp,
            )
        }
    }
}