package ai.create.photo.ui.gallery

import ai.create.photo.ui.gallery.creations.CreationsScreen
import ai.create.photo.ui.gallery.public.PublicScreen
import ai.create.photo.ui.gallery.uploads.UploadScreen
import ai.create.photo.ui.generate.Prompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.gallery_creations_tab
import photocreateai.composeapp.generated.resources.gallery_public_tab
import photocreateai.composeapp.generated.resources.gallery_uploads_tab


@Preview
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = viewModel { GalleryViewModel() },
    generationsInProgress: Int,
    openGenerateTab: (Prompt?) -> Unit,
    openTopUpTab: () -> Unit,
    openUploads: Boolean = false,
    openCreations: Boolean = false,
) {
    val state = viewModel.uiState
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {

        when (state.selectedTab) {
            0 -> PublicScreen(
                generate = { openGenerateTab(it) },
                addPhotosToPublicGallery = state.addPhotosToPublicGallery,
                onAddedPhotosToPublicGallery = viewModel::onAddedPhotoToPublicGallery,
                removePhotosFromPublicGallery = state.removePhotoFromPublicGallery,
                onRemovedPhotosFromPublicGallery = viewModel::onRemovedPhotoFromPublicGallery,
            )

            1 -> CreationsScreen(
                generate = { openGenerateTab(it) },
                generationsInProgress = generationsInProgress,
                addPhotoToPublicGallery = { viewModel.addPhotoToPublicGallery(it) },
                removePhotoFromPublicGallery = { viewModel.removePhotoFromPublicGallery(it) },
            )

            2 -> UploadScreen(
                openGenerateTab = { openGenerateTab(null) },
                openTopUpTab = openTopUpTab,
            )
        }

        Tabs(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            selectedTab = state.selectedTab
        ) {
            viewModel.selectTab(it)
        }
    }

    LaunchedEffect(openUploads) {
        if (openUploads) {
            viewModel.selectTab(2)
        }
    }

    LaunchedEffect(openCreations) {
        if (openCreations) {
            viewModel.selectTab(1)
        }
    }
}

@Composable
private fun Tabs(modifier: Modifier, selectedTab: Int, onClick: (Int) -> Unit) {
    val options = listOf(
        stringResource(Res.string.gallery_public_tab),
        stringResource(Res.string.gallery_creations_tab),
        stringResource(Res.string.gallery_uploads_tab),
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onClick(index) },
                selected = index == selectedTab
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
