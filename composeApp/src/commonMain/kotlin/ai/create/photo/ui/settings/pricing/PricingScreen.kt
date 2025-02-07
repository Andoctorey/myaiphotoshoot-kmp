package ai.create.photo.ui.settings.pricing

import ai.create.photo.platform.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.lowest_market_price
import photocreateai.composeapp.generated.resources.one_time_ai_training
import photocreateai.composeapp.generated.resources.photo_creation
import photocreateai.composeapp.generated.resources.powered_by_flux

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PricingScreen(
    viewModel: PricingViewModel = viewModel { PricingViewModel() },
    onBackClick: () -> Unit,
    trainAiModel: () -> Unit,
    openGenerateTab: () -> Unit,
) {

    BackHandler {
        onBackClick()
    }

    val state = viewModel.uiState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(state.scrollState)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        OutlinedCard {
            Column(modifier = Modifier.clickable { trainAiModel() }.padding(16.dp)) {
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.one_time_ai_training),
                        fontWeight = FontWeight.Light,
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$3.99–$7.99",
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.powered_by_flux),
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                )
            }
        }

        OutlinedCard {
            Column(modifier = Modifier.clickable { openGenerateTab() }.padding(16.dp)) {
                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.photo_creation),
                        fontWeight = FontWeight.Light,
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$0.03",
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.lowest_market_price),
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )
    }
}

