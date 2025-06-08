package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.model.GenerationsFilter
import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import ai.create.photo.platform.shareLink
import ai.create.photo.ui.compose.ConfirmationPopup
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.compose.PullToRefreshBoxNoDesktop
import ai.create.photo.ui.generate.Prompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.all_filter
import photocreateai.composeapp.generated.resources.cancel
import photocreateai.composeapp.generated.resources.copy_link
import photocreateai.composeapp.generated.resources.creations_placeholder
import photocreateai.composeapp.generated.resources.delete
import photocreateai.composeapp.generated.resources.delete_photo_confirmation
import photocreateai.composeapp.generated.resources.download
import photocreateai.composeapp.generated.resources.link_copied
import photocreateai.composeapp.generated.resources.make_private
import photocreateai.composeapp.generated.resources.make_public
import photocreateai.composeapp.generated.resources.prompt
import photocreateai.composeapp.generated.resources.public_filter
import photocreateai.composeapp.generated.resources.share


@Preview
@Composable
fun CreationsScreen(
    viewModel: CreationsViewModel = viewModel { CreationsViewModel() },
    generate: (Prompt?) -> Unit,
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
        } else if (state.photos.isEmpty()) {
            Placeholder(modifier = Modifier.align(Alignment.Center)) {
                generate(null)
            }
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
                onPhotoClick = viewModel::onPhotoClick,
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
                onDelete = viewModel::delete,
                onPrompt = generate,
            )

            Box(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 24.dp)
                    .safeDrawingPadding(),
            ) {
                Filter(
                    showDropDown = state.showFilterDropDownMenu,
                    onToggleMenu = { viewModel.toggleFilterDropDownMenu(it) },
                    filter = state.filter,
                    onFilter = { viewModel.filter(it) },
                )
            }
        }
    }

    if (state.errorPopup != null) {
        ErrorPopup(state.errorPopup) {
            viewModel.hideErrorPopup()
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 160.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.creations_placeholder),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
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
    onPhotoClick: (CreationsUiState.Photo) -> Unit,
    onDownload: (CreationsUiState.Photo) -> Unit,
    onTogglePublic: (CreationsUiState.Photo) -> Unit,
    onPrompt: (Prompt) -> Unit,
    onDelete: (CreationsUiState.Photo) -> Unit,
) {
    PullToRefreshBoxNoDesktop(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {

        val optimizedVersion = remember {
            platform().platform in listOf(
                Platforms.WEB_MOBILE,
                Platforms.WEB_DESKTOP,
            )
        }

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .then(if (!optimizedVersion || item < 3) Modifier.animateItem() else Modifier),
                ) {
                    Photo(
                        photo = photos[item],
                        optimizedVersion = optimizedVersion,
                        onPhotoClick = onPhotoClick,
                        onTogglePublic = onTogglePublic,
                        onDownload = { onDownload(photos[item]) },
                        onPrompt = onPrompt,
                        onDelete = { onDelete(photos[item]) },
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
                    if (lastVisibleItemIndex >= (photosCount - 3) && !isLoadingNextPage) {
                        loadNextPage()
                    }
                }
        }
    }
}

@Composable
private fun Photo(
    photo: CreationsUiState.Photo,
    optimizedVersion: Boolean,
    onPhotoClick: (CreationsUiState.Photo) -> Unit,
    onDownload: (CreationsUiState.Photo) -> Unit,
    onTogglePublic: (CreationsUiState.Photo) -> Unit,
    onPrompt: (Prompt) -> Unit,
    onDelete: (CreationsUiState.Photo) -> Unit,
) {
    var loaded by remember { mutableStateOf(false) }
    var showImage by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    error?.let {
        ErrorMessagePlaceHolderSmall(it)
    }

    if (optimizedVersion) {
        LaunchedEffect(photo.url) {
            delay(1000L)
            showImage = true
        }
    } else {
        showImage = true
    }

    Box(
        modifier = Modifier.fillMaxWidth().then(if (loaded) Modifier else Modifier.aspectRatio(1f))
    ) {
        if (showImage) {
            AsyncImage(
                modifier = Modifier.fillMaxWidth().clickable { onPhotoClick(photo) },
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photo.url)
                    .crossfade(!optimizedVersion)
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

        if (loaded) {
            PhotoDropMenu(
                modifier = Modifier.align(Alignment.TopEnd),
                photo = photo,
                onDownload = onDownload,
                onShare = { shareLink(photo.url) },
                onTogglePublic = onTogglePublic,
                onPrompt = onPrompt,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun PhotoDropMenu(
    modifier: Modifier,
    photo: CreationsUiState.Photo,
    onDownload: (CreationsUiState.Photo) -> Unit,
    onShare: (CreationsUiState.Photo) -> Unit,
    onTogglePublic: (CreationsUiState.Photo) -> Unit,
    onPrompt: (Prompt) -> Unit,
    onDelete: (CreationsUiState.Photo) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDeletePopup by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), shape = CircleShape),
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = Icons.Default.MoreVert.name)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.download)) },
                leadingIcon = {
                    if (!isDownloaded) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = Icons.Default.Download.name
                        )
                    } else {
                        Icon(
                            Icons.Default.DownloadDone,
                            contentDescription = Icons.Default.DownloadDone.name
                        )
                    }
                },
                onClick = {
                    expanded = false
                    isDownloaded = true
                    onDownload(photo)
                }
            )

            val copyLinkInsteadOfShare =
                remember { platform().platform !in listOf(Platforms.ANDROID, Platforms.IOS) }
            var linkCopied by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (linkCopied) stringResource(Res.string.link_copied)
                        else if (copyLinkInsteadOfShare) stringResource(Res.string.copy_link)
                        else stringResource(Res.string.share)
                    )
                },
                leadingIcon = {
                    val icon = if (linkCopied) Icons.Default.Check
                    else if (copyLinkInsteadOfShare) Icons.Default.Link
                    else Icons.Default.Share
                    Icon(icon, contentDescription = icon.name)
                },
                onClick = {
                    if (copyLinkInsteadOfShare) {
                        linkCopied = true
                        coroutineScope.launch {
                            delay(1500)
                            linkCopied = false
                            expanded = false
                        }
                    } else {
                        expanded = false
                    }
                    onShare(photo)
                },
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (photo.isPublic) Res.string.make_private
                            else Res.string.make_public
                        )
                    )
                },
                leadingIcon = {
                    val icon = if (photo.isPublic) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility
                    Icon(
                        imageVector = icon,
                        contentDescription = icon.name,
                    )
                },
                onClick = {
                    expanded = false
                    onTogglePublic(photo)
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.prompt)
                    )
                },
                leadingIcon = {
                    val icon = Icons.AutoMirrored.Default.Send
                    Icon(
                        imageVector = icon,
                        contentDescription = icon.name,
                    )
                },
                onClick = {
                    expanded = false
                    onPrompt(Prompt(generationId = photo.id, text = photo.prompt, url = photo.url))
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = Icons.Default.Delete.name,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    showConfirmDeletePopup = true
                }
            )

            if (showConfirmDeletePopup) {
                ConfirmationPopup(
                    icon = Icons.Default.Delete,
                    message = stringResource(Res.string.delete_photo_confirmation),
                    confirmButton = stringResource(Res.string.delete),
                    dismissButton = stringResource(Res.string.cancel),
                    onConfirm = { onDelete(photo) },
                    onDismiss = { showConfirmDeletePopup = false }
                )
            }
        }
    }
}

@Composable
fun Filter(
    showDropDown: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    filter: GenerationsFilter,
    onFilter: (GenerationsFilter) -> Unit,
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
                val name = stringResource(Res.string.all_filter)
                val color = if (filter == GenerationsFilter.ALL)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
                Row {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = name,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = color,
                    )
                    Text(
                        text = name,
                        color = color,
                        fontWeight = if (filter == GenerationsFilter.ALL)
                            FontWeight.Bold else FontWeight.Normal,
                    )
                }
            },
            onClick = {
                onFilter(GenerationsFilter.ALL)
                onToggleMenu(false)
            },
        )
        DropdownMenuItem(
            text = {
                val name = stringResource(Res.string.public_filter)
                val color = if (filter == GenerationsFilter.PUBLIC)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
                Row {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = name,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = color,
                    )
                    Text(
                        text = name,
                        color = color,
                        fontWeight = if (filter == GenerationsFilter.PUBLIC)
                            FontWeight.Bold else FontWeight.Normal,
                    )
                }
            },
            onClick = {
                onFilter(GenerationsFilter.PUBLIC)
                onToggleMenu(false)
            },
        )
    }
}
