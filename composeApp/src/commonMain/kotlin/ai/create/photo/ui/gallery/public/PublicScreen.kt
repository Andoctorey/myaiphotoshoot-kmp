package ai.create.photo.ui.gallery.public

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun PublicScreen(
    viewModel: PublicViewModel = viewModel { PublicViewModel() },
) {
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
        } else {
            Photos(
                photos = state.photos,
                listState = state.listState,
                isLoadingNextPage = state.isLoadingNextPage,
                loadNextPage = viewModel::loadPublicGallery,
                pagingLimitReach = state.pagingLimitReach,
            )
        }
    }

    if (state.errorPopup != null) {
        ErrorPopup(state.errorPopup) {
            viewModel.hideErrorPopup()
        }
    }
}

@Composable
private fun Photos(
    photos: List<PublicUiState.Photo>,
    listState: LazyStaggeredGridState,
    isLoadingNextPage: Boolean,
    pagingLimitReach: Boolean,
    loadNextPage: () -> Unit = {},
) {
    val density = LocalDensity.current
    val width = 320
    val minSize = remember { with(density) { (width - 20).toDp() } } // paddings
    LazyVerticalStaggeredGrid(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(minSize = minSize),
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
                width = width,
            )
        }

        if (isLoadingNextPage && !pagingLimitReach) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingPlaceholder()
                }
            }
        }
    }

    val photosCount = photos.size
    if (!isLoadingNextPage && !pagingLimitReach) {
        LaunchedEffect(listState, photosCount) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .collect {
                    val lastVisibleItemIndex = it ?: return@collect
                    if (lastVisibleItemIndex >= (photosCount - 3) && !isLoadingNextPage) {
                        loadNextPage()
                    }
                }
        }
    }
}

@Composable
private fun Photo(
    modifier: Modifier,
    photo: PublicUiState.Photo,
    width: Int,
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Throwable?>(null) }

    if (error != null) {
        Box(
            modifier = modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            ErrorMessagePlaceHolderSmall(error!!)
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
                .data(photo.url + "?width=$width")
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
