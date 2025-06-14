package ai.create.photo.ui.gallery

import ai.create.photo.ui.gallery.creations.CreationsScreen
import ai.create.photo.ui.gallery.public.PublicScreen
import ai.create.photo.ui.gallery.uploads.UploadScreen
import ai.create.photo.ui.generate.Prompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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


@Preview
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = viewModel { GalleryViewModel() },
    generationsInProgress: Int,
    openGenerateTab: (Prompt?) -> Unit,
    openUploads: Boolean = false,
    openCreations: Boolean = false,
) {
    val state = viewModel.uiState
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {

        when (state.selectedTab) {
            Tab.PUBLIC -> PublicScreen(
                generate = { openGenerateTab(it) },
                addPhotosToPublicGallery = state.addPhotosToPublicGallery,
                onAddedPhotosToPublicGallery = viewModel::onAddedPhotoToPublicGallery,
                removePhotosFromPublicGallery = state.removePhotoFromPublicGallery,
                onRemovedPhotosFromPublicGallery = viewModel::onRemovedPhotoFromPublicGallery,
            )

            Tab.CREATIONS -> CreationsScreen(
                generate = { openGenerateTab(it) },
                generationsInProgress = generationsInProgress,
                addPhotoToPublicGallery = { viewModel.addPhotoToPublicGallery(it) },
                removePhotoFromPublicGallery = { viewModel.removePhotoFromPublicGallery(it) },
            )

            Tab.UPLOADS -> UploadScreen(
                openGenerateTab = { openGenerateTab(null) },
            )
        }

        Tabs(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).safeDrawingPadding(),
            selectedTab = state.selectedTab,
            publicTabFirst = state.firstTrainingCompleted == true,
        ) {
            viewModel.selectTab(it)
        }
    }

    LaunchedEffect(openUploads) {
        if (openUploads) {
            viewModel.selectTab(Tab.UPLOADS)
        }
    }

    LaunchedEffect(openCreations) {
        if (openCreations) {
            viewModel.selectTab(Tab.CREATIONS)
        }
    }
}

@Composable
private fun Tabs(
    modifier: Modifier,
    selectedTab: Tab,
    publicTabFirst: Boolean,
    onClick: (Tab) -> Unit,
) {
    val tabs = if (publicTabFirst) {
        listOf(Tab.PUBLIC, Tab.CREATIONS, Tab.UPLOADS)
    } else {
        listOf(Tab.UPLOADS, Tab.CREATIONS, Tab.PUBLIC)
    }
    val options = tabs.map { stringResource(it.label) }.toTypedArray()
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        tabs.forEachIndexed { index, tab ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onClick(tab) },
                selected = tab == selectedTab
            ) {
                Text(
                    text = stringResource(tab.label),
                    maxLines = 1,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
