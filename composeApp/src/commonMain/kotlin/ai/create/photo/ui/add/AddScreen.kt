package ai.create.photo.ui.add

import ai.create.photo.supabase.model.TrainingStatus
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.compose.getFriendlyError
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.add_your_photos
import photocreateai.composeapp.generated.resources.create_ai_model
import photocreateai.composeapp.generated.resources.create_photo_set
import photocreateai.composeapp.generated.resources.create_photos
import photocreateai.composeapp.generated.resources.creating_ai_model
import photocreateai.composeapp.generated.resources.delete_photo_set
import photocreateai.composeapp.generated.resources.photo_set
import photocreateai.composeapp.generated.resources.upload_guidelines_message
import photocreateai.composeapp.generated.resources.upload_more_photos


@Preview
@Composable
fun AddScreen(
    viewModel: AddViewModel = viewModel { AddViewModel() },
    generatePhotos: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val launcher = rememberFilePickerLauncher(
            title = stringResource(Res.string.add_your_photos),
            type = PickerType.Image,
            mode = PickerMode.Multiple()
        ) { files ->
            if (files == null) return@rememberFilePickerLauncher
            viewModel.uploadPhotos(files)
        }
        val onAddPhotoClick = { launcher.launch() }

        val state = viewModel.uiState

        val photos = state.displayingPhotos
        if (state.isLoadingPhotos) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else if (photos.isNullOrEmpty()) {
            Placeholder(modifier = Modifier.align(Alignment.Center))
        } else {
            LaunchedEffect(state.scrollToTop) {
                if (state.scrollToTop && state.listState.firstVisibleItemIndex > 1) {
                    state.listState.animateScrollToItem(0)
                }
                viewModel.resetScrollToTop()
            }
            Photos(photos, state.listState) {
                viewModel.deletePhoto(it)
            }
        }

        if (state.isLoadingTraining) {
            LoadingPlaceholder(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            )
        } else {
            if ((photos?.size ?: 0) >= 10) {
                CreateModelFab(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                    extended = true,
                    trainingStatus = state.trainingStatus,
                    createModel = viewModel::createModel,
                    generatePhotos = generatePhotos,
                )
            } else {
                AddPhotosFab(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                    extended = true,
                    uploadProgress = state.uploadProgress,
                    onClick = onAddPhotoClick,
                )
            }

            FabMenu(
                modifier = Modifier.align(Alignment.BottomEnd),
                photos = state.displayingPhotos,
                photoSets = state.photoSets,
                uploadProgress = state.uploadProgress,
                trainingStatus = state.trainingStatus,
                showMenu = state.showMenu,
                onAddPhotoClick = onAddPhotoClick,
                toggleMenu = viewModel::toggleMenu,
                createModel = viewModel::createModel,
                generatePhotos = generatePhotos,
                photoSet = state.photoSet,
                selectPhotoSet = viewModel::selectPhotoSet,
                createPhotoSet = viewModel::createPhotoSet,
                deletePhotoSet = viewModel::deletePhotoSet,
            )
        }

        if (state.showUploadMorePhotosPopup) {
            ShowUploadMorePhotosPopup {
                viewModel.hideUploadMorePhotosPopup()
            }
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier) {
    Column(
        modifier = modifier.widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState()).safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 82.dp), // fab
    ) {
        Text(
            text = stringResource(Res.string.upload_guidelines_message),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AddPhotosFab(
    modifier: Modifier = Modifier,
    extended: Boolean = false,
    uploadProgress: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        val isLoading = uploadProgress in 1 until 100
        ExtendedFloatingActionButton(
            onClick = { if (!isLoading) onClick() },
        ) {
            if (uploadProgress in 1 until 100) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uploadProgress}%",
                        fontSize = 12.sp,
                    )
                    LinearProgressIndicator(progress = { uploadProgress / 100f })
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (extended) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = stringResource(Res.string.add_your_photos),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Text(
                        text = stringResource(Res.string.add_your_photos),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = if (extended) 14.sp else 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun CreateModelFab(
    modifier: Modifier = Modifier,
    extended: Boolean = false,
    trainingStatus: TrainingStatus?,
    createModel: () -> Unit,
    generatePhotos: () -> Unit,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        ExtendedFloatingActionButton(
            onClick = {
                when (trainingStatus) {
                    TrainingStatus.SUCCEEDED -> generatePhotos()
                    TrainingStatus.PROCESSING -> {}
                    null -> createModel()
                }
            },
        ) {
            when (trainingStatus) {
                TrainingStatus.SUCCEEDED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (extended) {
                                Icon(
                                    imageVector = Icons.Default.ModeEdit,
                                    contentDescription = stringResource(Res.string.create_ai_model),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Text(
                                text = stringResource(Res.string.create_photos),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = if (extended) 14.sp else 13.sp,
                            )
                        }
                    }
                }

                TrainingStatus.PROCESSING -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LoadingPlaceholder()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(Res.string.creating_ai_model),
                        )
                    }
                }

                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (extended) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = stringResource(Res.string.create_ai_model),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        Text(
                            text = stringResource(Res.string.create_ai_model),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = if (extended) 14.sp else 13.sp,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun Photos(
    photos: List<AddUiState.Photo>,
    listState: LazyStaggeredGridState,
    onDelete: (AddUiState.Photo) -> Unit,
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
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun Photo(
    modifier: Modifier,
    photo: AddUiState.Photo,
    onDelete: (AddUiState.Photo) -> Unit
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
                Logger.e("error loading image", it.result.throwable)
                error = it.result.throwable
            },
            contentDescription = "photo",
        )

        if (!loading && error == null) {
            IconButton(
                onClick = { onDelete(photo) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "delete",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
fun FabMenu(
    modifier: Modifier,
    photos: List<AddUiState.Photo>?,
    uploadProgress: Int,
    trainingStatus: TrainingStatus?,
    showMenu: Boolean,
    onAddPhotoClick: () -> Unit,
    toggleMenu: () -> Unit,
    createModel: () -> Unit,
    generatePhotos: () -> Unit,
    photoSet: Int,
    photoSets: List<Int>?,
    selectPhotoSet: (Int) -> Unit,
    createPhotoSet: () -> Unit,
    deletePhotoSet: () -> Unit,
) {
    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.End) {
        Crossfade(targetState = showMenu) {
            if (!it) return@Crossfade
            Column(horizontalAlignment = Alignment.End) {
                if (photoSets != null && (photoSets.isNotEmpty() || (photos?.size ?: 0) >= 1)) {
                    PhotoSets(
                        photoSets = photoSets,
                        selectedPhotoSet = photoSet,
                        selectPhotoSet = selectPhotoSet,
                        createPhotoSet = createPhotoSet
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExtendedFloatingActionButton(onClick = deletePhotoSet) {
                        Text(
                            text = stringResource(Res.string.delete_photo_set),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = 13.sp,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if ((photos?.size ?: 0) <= 30) {
                    if ((photos?.size ?: 0) >= 10) {
                        AddPhotosFab(
                            uploadProgress = uploadProgress,
                            onClick = onAddPhotoClick
                        )
                    } else {
                        CreateModelFab(
                            trainingStatus = trainingStatus,
                            createModel = createModel,
                            generatePhotos = generatePhotos,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        SmallFloatingActionButton(
            onClick = toggleMenu,
        ) {
            Icon(
                imageVector = if (showMenu) Icons.Default.Close else Icons.Default.Settings,
                contentDescription = "settings",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSets(
    photoSets: List<Int>,
    selectedPhotoSet: Int,
    selectPhotoSet: (Int) -> Unit,
    createPhotoSet: () -> Unit
) {
    val options = photoSets.toMutableList()
    options.add(0)
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(selectedPhotoSet) }
    val photoSetString = stringResource(Res.string.photo_set)
    val createPhotoSetString = stringResource(Res.string.create_photo_set)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        ExtendedFloatingActionButton(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            onClick = { },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedOption == 0) createPhotoSetString
                    else "$photoSetString $selectedOption",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "error",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (option == 0) createPhotoSetString
                            else "$photoSetString $option",
                        )
                    },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        if (selectedOption == 0) createPhotoSet()
                        else selectPhotoSet(selectedOption)
                    }
                )
            }
        }
    }
}


@Composable
private fun ShowUploadMorePhotosPopup(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = stringResource(Res.string.upload_more_photos)) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun ErrorPopup(e: Throwable, onDismiss: () -> Unit) {
    AlertDialog(
        icon = { Icon(Icons.Default.Error, contentDescription = "error") },
        onDismissRequest = onDismiss,
        text = {
            Text(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                text = e.getFriendlyError()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
