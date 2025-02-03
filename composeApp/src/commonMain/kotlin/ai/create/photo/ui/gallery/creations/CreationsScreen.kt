package ai.create.photo.ui.gallery.creations

import GeneratedPhotoSharing
import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.compose.PhotoDropMenu
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
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
fun CreationsScreen(
    viewModel: CreationsViewModel = viewModel { CreationsViewModel() },
    generationsInProgress: Int,
    addPhotoToPublicGallery: (UserGeneration) -> Unit,
    removePhotoFromPublicGallery: (String) -> Unit,
) {
    var previousGenerationsInProgress by remember { mutableStateOf(0) }
    LaunchedEffect(generationsInProgress) {
        if (generationsInProgress != previousGenerationsInProgress) {
            viewModel.refreshCreations()
            previousGenerationsInProgress = generationsInProgress
        } else {
            viewModel.refreshCreations(silent = true)
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
        } else {
            LaunchedEffect(state.scrollToTop) {
                if (state.scrollToTop && state.listState.firstVisibleItemIndex > 1) {
                    state.listState.animateScrollToItem(0)
                }
                viewModel.resetScrollToTop()
            }

            Photos(
                photos = state.photos,
                listState = state.listState,
                isLoadingNextPage = state.isLoadingNextPage,
                pagingLimitReach = state.pagingLimitReach,
                loadNextPage = viewModel::loadCreations,
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refreshCreations,
                onDelete = viewModel::delete,
                onTogglePublic = {
                    viewModel.togglePublic(it) {
                        if (it.isPublic) {
                            removePhotoFromPublicGallery(it.id)
                        } else {
                            addPhotoToPublicGallery(it.toUserGeneration())
                        }
                    }
                },
                onDownload = viewModel::downloadGeneratedPhoto,
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
    photos: List<CreationsUiState.Photo>,
    listState: LazyGridState,
    isLoadingNextPage: Boolean,
    pagingLimitReach: Boolean,
    loadNextPage: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit,
    onDelete: (CreationsUiState.Photo) -> Unit,
    onDownload: (CreationsUiState.Photo) -> Unit,
    onTogglePublic: (CreationsUiState.Photo) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyVerticalGrid(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Adaptive(minSize = 540.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
            }

            items(photos.size, key = { photos[it].id }) { item ->
                Photo(
                    modifier = Modifier.animateItem(),
                    photo = photos[item],
                    onDelete = { onDelete(photos[item]) },
                    onTogglePublic = onTogglePublic,
                    onDownload = { onDownload(photos[item]) },
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
    photo: CreationsUiState.Photo,
    onDownload: (CreationsUiState.Photo) -> Unit,
    onDelete: (CreationsUiState.Photo) -> Unit,
    onTogglePublic: (CreationsUiState.Photo) -> Unit,
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

        val snackbarHostState = remember { SnackbarHostState() }

        PhotoDropMenu(
            modifier = modifier.align(Alignment.TopEnd),
            item = photo,
            onDelete = onDelete,
            onShare = {
                GeneratedPhotoSharing().sharePhoto(photo.url)
            },
            onTogglePublic = onTogglePublic,
            onDownload = onDownload,
            snackbarHostState = snackbarHostState
        )
        if (platform().platform != Platforms.ANDROID || platform().platform != Platforms.IOS) {
            Box(contentAlignment = Alignment.BottomCenter) {
                SnackbarHost(hostState = snackbarHostState) {
                    Snackbar(
                        snackbarData = it,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
