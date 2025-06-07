package ai.create.photo.ui.settings.balance

import ai.create.photo.platform.BackHandler
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import photocreateai.composeapp.generated.resources.lowest_market_price
import photocreateai.composeapp.generated.resources.one_time_ai_training
import photocreateai.composeapp.generated.resources.photo_creation
import photocreateai.composeapp.generated.resources.powered_by_flux
import photocreateai.composeapp.generated.resources.pricing
import photocreateai.composeapp.generated.resources.promo_code_applied
import photocreateai.composeapp.generated.resources.thank_you_for_purchase
import photocreateai.composeapp.generated.resources.top_up
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
                    .verticalScroll(state = state.scrollState)
                    .animateContentSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Card(
                    modifier = Modifier.padding(8.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Pricing(
                        trainAiModel = trainAiModel,
                        openGenerateTab = openGenerateTab
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.padding(8.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    TopUp(
                        topUp = viewModel::topUp,
                        showEnterPromoCode = state.showEnterPromoCode,
                        promoCode = state.promoCode,
                        isIncorrectPromoCode = state.isIncorrectPromoCode,
                        onPromoCodeChanged = viewModel::onPromoCodeChanged,
                        isApplyingPromoCode = state.isApplyingPromoCode,
                        applyPromoCode = viewModel::applyPromoCode,
                        enterPromoCode = viewModel::enterPromoCode,
                    )
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

        if (state.showBalanceUpdatedPopup) {
            InfoPopup(stringResource(Res.string.thank_you_for_purchase, state.balance)) {
                viewModel.hideBalanceUpdatedPopup()
                onBackClick()
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

@Composable
fun TopUpButton(pricing: Pricing, onClick: (String) -> Unit) {
    Button(
        modifier = Modifier.padding(4.dp),
        onClick = { onClick(pricing.paymentLink) }) {
        Text(
            text = pricing.price,
            fontSize = 24.sp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopUp(
    topUp: (Pricing) -> Unit,
    showEnterPromoCode: Boolean,
    promoCode: String,
    isIncorrectPromoCode: Boolean,
    onPromoCodeChanged: (String) -> Unit,
    isApplyingPromoCode: Boolean,
    applyPromoCode: () -> Unit,
    enterPromoCode: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.top_up),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
        ) {
            TopUpButton(Pricing.STARTER) { topUp(Pricing.STARTER) }
            TopUpButton(Pricing.CREATIVE) { topUp(Pricing.CREATIVE) }
            TopUpButton(Pricing.FAMILY) { topUp(Pricing.FAMILY) }
        }


        if (platform().platform != Platforms.IOS) {
            Spacer(modifier = Modifier.height(8.dp))

            Crossfade(showEnterPromoCode) {
                if (showEnterPromoCode) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EnterPromoCode(
                            promoCode = promoCode,
                            isIncorrectCode = isIncorrectPromoCode,
                            onCodeChanged = onPromoCodeChanged,
                        )
                        ApplyPromoCodeButton(
                            isLoading = isApplyingPromoCode,
                            onClick = applyPromoCode,
                        )
                    }
                } else {
                    TextButton(onClick = { enterPromoCode() }) {
                        Text(
                            text = stringResource(Res.string.enter_promo_code),
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Pricing(trainAiModel: () -> Unit, openGenerateTab: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.pricing),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors().copy(
                containerColor = Color.Transparent,
            )
        ) {
            Column(modifier = Modifier.clickable { trainAiModel() }.padding(16.dp)) {
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.one_time_ai_training),
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$3.99+",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.powered_by_flux),
                    fontSize = 14.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors().copy(
                containerColor = Color.Transparent,
            )
        ) {
            Column(modifier = Modifier.clickable { openGenerateTab() }.padding(16.dp)) {
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.photo_creation),
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$0.03",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.lowest_market_price),
                    fontSize = 14.sp,
                )
            }
        }
    }
}