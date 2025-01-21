package ai.create.photo.ui.gallery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GalleryViewModel : ViewModel() {

    var uiState by mutableStateOf(GalleryUiState())
        private set

    fun selectTab(tab: Int) {
        uiState = uiState.copy(selectedTab = tab)
    }
}
