package ai.create.photo.ui.gallery

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
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
    generationInProgress: Boolean,
) {
    LaunchedEffect(generationInProgress) {
        if (!generationInProgress) {
            viewModel.loadGallery()
        }
    }

    val state = viewModel.uiState
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isLoading) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else if (state.photos != null) {
            LaunchedEffect(state.scrollToTop) {
                if (state.scrollToTop && state.listState.firstVisibleItemIndex > 1) {
                    state.listState.animateScrollToItem(0)
                }
                viewModel.resetScrollToTop()
            }

            Photos(state.photos, state.listState)

            Tabs(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
private fun Tabs(modifier: Modifier) {
    var selectedIndex by remember { mutableStateOf(0) }
    val options = listOf(
        stringResource(Res.string.gallery_public_tab),
        stringResource(Res.string.gallery_creations_tab),
        stringResource(Res.string.gallery_uploads_tab),
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { selectedIndex = index },
                selected = index == selectedIndex
            ) {
                Text(
                    text = label,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun Photos(
    photos: List<GalleryUiState.Photo>,
    listState: LazyStaggeredGridState,
) {
    LazyVerticalStaggeredGrid(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(minSize = 540.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        }

        items(photos.size, key = { photos[it].id }) { item ->
            Photo(
                modifier = Modifier.animateItem(),
                photo = photos[item],
            )
        }
    }
}

@Composable
private fun Photo(
    modifier: Modifier,
    photo: GalleryUiState.Photo,
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Throwable?>(null) }

    if (error != null) {
        Box(
            modifier = modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            ErrorMessagePlaceHolder(error!!)
        }
    } else if (loading) {
        Box(
            modifier = modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            LoadingPlaceholder()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photo.url)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            onSuccess = { loading = false },
            onError = {
                Logger.e("error loading image ${photo.url}", it.result.throwable)
                error = it.result.throwable
            },
            contentDescription = "photo",
        )
    }
}
