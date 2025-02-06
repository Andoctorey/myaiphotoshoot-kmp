package ai.create.photo.ui.settings.pricing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PricingViewModel : ViewModel() {

    var uiState by mutableStateOf(PricingUiState())
        private set

}
