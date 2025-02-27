package ai.create.photo.ui.settings.balance

import ai.create.photo.platform.BackHandler
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.settings.pricing.PricingPopup
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import photocreateai.composeapp.generated.resources.pricing
import photocreateai.composeapp.generated.resources.promo_code_applied
import photocreateai.composeapp.generated.resources.wrong_code

@Composable
fun BalanceScreen(
    viewModel: BalanceViewModel = viewModel { BalanceViewModel() },
    trainAiModel: () -> Unit,
    openGenerateTab: () -> Unit,
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
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(state = state.scrollState)
                    .animateContentSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextButton(onClick = { viewModel.togglePricingPopup(true) }) {
                    Text(
                        text = stringResource(Res.string.pricing),
                        fontSize = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                TopUpButton(Pricing.STARTER) { viewModel.topUp(it) }
                Spacer(modifier = Modifier.height(16.dp))
                TopUpButton(Pricing.CREATIVE) { viewModel.topUp(it) }
                Spacer(modifier = Modifier.height(16.dp))
                TopUpButton(Pricing.FAMILY) { viewModel.topUp(it) }

                Spacer(modifier = Modifier.height(16.dp))

                Crossfade(state.enterPromoCode) {
                    if (state.enterPromoCode) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    } else {
                        TextButton(onClick = { viewModel.enterPromoCode() }) {
                            Text(
                                text = stringResource(Res.string.enter_promo_code),
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
            }
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }

        if (state.showPromoCodeAppliedPopup) {
            InfoPopup(stringResource(Res.string.promo_code_applied, state.balance)) {
                viewModel.hidePromoCodeAppliedPopup()
                onBackClick()
            }
        }

        if (state.showPricingPopup) {
            PricingPopup(
                trainAiModel = trainAiModel,
                openGenerateTab = openGenerateTab,
                onDismiss = { viewModel.togglePricingPopup(false) }
            )
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

@Composable
fun TopUpButton(pricing: Pricing, onClick: (String) -> Unit) {
    OutlinedCard {
        Column(modifier = Modifier.clickable { onClick(pricing.paymentLink) }.padding(16.dp)) {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(pricing.title),
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = pricing.price,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            pricing.descriptions.forEach {
                Text(
                    text = stringResource(it),
                    fontSize = 14.sp,
                )
            }
        }
    }
}