package ai.create.photo.ui.settings.pricing

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Immutable

@Immutable
data class PricingUiState(
    val scrollState: ScrollState = ScrollState(0),
)