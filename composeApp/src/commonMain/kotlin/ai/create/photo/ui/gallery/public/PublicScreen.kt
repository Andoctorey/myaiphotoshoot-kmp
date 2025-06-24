package ai.create.photo.ui.gallery.public

import ai.create.photo.data.supabase.model.GenerationsSort
import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.compose.PullToRefreshBoxNoDesktop
import ai.create.photo.ui.generate.Prompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.newest_sort_order
import photocreateai.composeapp.generated.resources.popular_sort_order
import photocreateai.composeapp.generated.resources.upload_tooltip_popup_message
import photocreateai.composeapp.generated.resources.upload_tooltip_popup_title


@Preview
@Composable
fun PublicScreen(
    viewModel: PublicViewModel = viewModel { PublicViewModel() },
    generate: (Prompt) -> Unit,
    addPhotosToPublicGallery: List<UserGeneration>,
    onAddedPhotosToPublicGallery: () -> Unit,
    removePhotosFromPublicGallery: List<String>,
    onRemovedPhotosFromPublicGallery: () -> Unit,
) {
    val state = viewModel.uiState

    if (state.sort == GenerationsSort.NEW) {
        LaunchedEffect(Unit) {
            viewModel.loadLatestPublicGallerySortedByNew(silent = true)
        }
    }

    if (addPhotosToPublicGallery.isNotEmpty()) {
        viewModel.addPhotoToPublicGallery(addPhotosToPublicGallery)
        onAddedPhotosToPublicGallery()
    }

    if (removePhotosFromPublicGallery.isNotEmpty()) {
        viewModel.removePhotoFromPublicGallery(removePhotosFromPublicGallery)
        onRemovedPhotosFromPublicGallery()
    }

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
                onRefresh = viewModel::onRefreshPublicGallery,
                pagingLimitReach = state.pagingLimitReach,
                onClick = { generate(Prompt(generationId = it.id, text = it.prompt, url = it.url)) }
            )

            Box(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 24.dp)
                    .safeDrawingPadding(),
            ) {
                SortButton(
                    showDropDown = state.showSortDropDownMenu,
                    onToggleMenu = { viewModel.toggleSortDropDownMenu(it) },
                    sort = state.sort,
                    onSort = { viewModel.sort(it) },
                )
            }
        }
    }

    if (state.errorPopup != null) {
        ErrorPopup(state.errorPopup) {
            viewModel.hideErrorPopup()
        }
    }

    if (state.showTooltipPopup) {
        InfoPopup(
            icon = Icons.Default.Stars,
            title = stringResource(Res.string.upload_tooltip_popup_title),
            message = stringResource(Res.string.upload_tooltip_popup_message),
        ) {
            viewModel.toggleTooltipPopup(false)
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
    PullToRefreshBoxNoDesktop(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        val density = LocalDensity.current
        val width = 420
        val minSize = remember { with(density) { (width - 20).toDp() } } // paddings
        val optimizedVersion = remember {
            platform().platform in listOf(
                Platforms.WEB_MOBILE,
                Platforms.WEB_DESKTOP,
            )
        }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .animateItem(),
                ) {
                    Photo(
                        photo = photos[item],
                        doNotLoad = optimizedVersion && listState.isScrollInProgress,
                        width = width,
                        onClick = onClick,
                    )
                }
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

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
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
    photo: PublicUiState.Photo,
    doNotLoad: Boolean,
    @Suppress("SameParameterValue") width: Int,
    onClick: (PublicUiState.Photo) -> Unit,
) {
    var loaded by remember { mutableStateOf(false) }
    if (!loaded && doNotLoad) return

    var error by remember { mutableStateOf<Throwable?>(null) }
    error?.let {
        ErrorMessagePlaceHolderSmall(it)
    }

    AsyncImage(
        modifier = Modifier.fillMaxSize().clickable { onClick(photo) },
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(photo.url + if (photo.url.contains("b-cdn.net") || photo.url.contains("cdn.myaiphotoshoot.com")) "?width=$width" else "")
            .crossfade(true)
            .build(),
        contentDescription = photo.prompt,
        contentScale = ContentScale.FillWidth,
        onSuccess = { loaded = true },
        onError = {
            Logger.e("error loading image ${photo.url}", it.result.throwable)
            error = it.result.throwable
        },
    )
}

@Composable
fun SortButton(
    showDropDown: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    sort: GenerationsSort,
    onSort: (GenerationsSort) -> Unit,
) {
    SmallFloatingActionButton(
        onClick = { onToggleMenu(true) },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.Sort,
            contentDescription = Icons.Default.AddAPhoto.name,
        )
    }

    DropdownMenu(
        expanded = showDropDown,
        onDismissRequest = { onToggleMenu(false) },
    ) {
        DropdownMenuItem(
            text = {
                val name = stringResource(Res.string.popular_sort_order)
                val color = if (sort == GenerationsSort.POPULAR)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
                Row {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = name,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = color,
                    )
                    Text(
                        text = name,
                        color = color,
                        fontWeight = if (sort == GenerationsSort.POPULAR)
                            FontWeight.Bold else FontWeight.Normal,
                    )
                }
            },
            onClick = {
                onSort(GenerationsSort.POPULAR)
                onToggleMenu(false)
            },
        )
        DropdownMenuItem(
            text = {
                val name = stringResource(Res.string.newest_sort_order)
                val color = if (sort == GenerationsSort.NEW)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
                Row {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = name,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = color,
                    )
                    Text(
                        text = name,
                        color = color,
                        fontWeight = if (sort == GenerationsSort.NEW)
                            FontWeight.Bold else FontWeight.Normal,
                    )
                }
            },
            onClick = {
                onSort(GenerationsSort.NEW)
                onToggleMenu(false)
            },
        )
    }
}
