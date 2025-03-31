package ai.create.photo.ui.gallery

import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.gallery_creations_tab
import photocreateai.composeapp.generated.resources.gallery_public_tab
import photocreateai.composeapp.generated.resources.gallery_uploads_tab

@Immutable
data class GalleryUiState(
    val selectedTab: Tab = Tab.UPLOADS,
    val addPhotosToPublicGallery: List<UserGeneration> = emptyList(),
    val removePhotoFromPublicGallery: List<String> = emptyList(),
    val firstTrainingCompleted: Boolean? = null,
)

enum class Tab(val label: StringResource) {
    UPLOADS(Res.string.gallery_uploads_tab),
    CREATIONS(Res.string.gallery_creations_tab),
    PUBLIC(Res.string.gallery_public_tab),
}
