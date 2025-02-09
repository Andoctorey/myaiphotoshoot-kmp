package ai.create.photo.ui.gallery.public

import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
    generate: (String) -> Unit,
    addPhotosToPublicGallery: List<UserGeneration>,
    onAddedPhotosToPublicGallery: () -> Unit,
    removePhotosFromPublicGallery: List<String>,
    onRemovedPhotosFromPublicGallery: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.refreshPublicGallery(silent = true)
    }

    if (addPhotosToPublicGallery.isNotEmpty()) {
        viewModel.addPhotoToPublicGallery(addPhotosToPublicGallery)
        onAddedPhotosToPublicGallery()
    }

    if (removePhotosFromPublicGallery.isNotEmpty()) {
        viewModel.removePhotoFromPublicGallery(removePhotosFromPublicGallery)
        onRemovedPhotosFromPublicGallery()
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
        } else {
            Photos(
                photos = state.photos,
                listState = state.listState,
                isLoadingNextPage = state.isLoadingNextPage,
                loadNextPage = viewModel::loadPublicGallery,
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refreshPublicGallery,
                pagingLimitReach = state.pagingLimitReach,
                onClick = { generate(it.prompt) }
            )
        }
    }

    if (state.errorPopup != null) {
        ErrorPopup(state.errorPopup) {
            viewModel.hideErrorPopup()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Photos(
    photos: List<PublicUiState.Photo>,
    listState: LazyGridState,
    isLoadingNextPage: Boolean,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit,
    pagingLimitReach: Boolean,
    loadNextPage: () -> Unit = {},
    onClick: (PublicUiState.Photo) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        val density = LocalDensity.current
        val width = 320
        val minSize = remember { with(density) { (width - 20).toDp() } } // paddings
        LazyVerticalGrid(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(minSize = minSize),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
            }

            items(photos.size, key = { photos[it].id }) { item ->
                Photo(
                    modifier = Modifier.animateItem(),
                    photo = photos[item],
                    width = width,
                    onClick = onClick,
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
    }

    val photosCount = photos.size
    if (!isLoadingNextPage && !pagingLimitReach) {
        LaunchedEffect(listState, photosCount) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .distinctUntilChanged()
                .collect {
                    val lastVisibleItemIndex = it ?: return@collect
                    if (lastVisibleItemIndex >= (photosCount - 30) && !isLoadingNextPage) {
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
    onClick: (PublicUiState.Photo) -> Unit,
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
            val icon = Icons.Default.Cached
            Icon(icon, contentDescription = icon.name)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth().clickable { onClick(photo) },
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photo.url + if (photo.url.contains("b-cdn.net")) "?width=$width" else "")
                .crossfade(true)
                .build(),
            contentDescription = photo.prompt,
            contentScale = ContentScale.FillWidth,
            onSuccess = { loading = false },
            onError = {
                loading = false
                Logger.e("error loading image ${photo.url}", it.result.throwable)
                error = it.result.throwable
            },
        )
    }
}
