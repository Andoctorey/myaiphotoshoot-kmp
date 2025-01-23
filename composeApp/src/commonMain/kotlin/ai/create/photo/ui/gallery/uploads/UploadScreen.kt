package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import ai.create.photo.ui.compose.ConfirmationPopup
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.training.TrainAiModelPopup
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
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
import photocreateai.composeapp.generated.resources.analyze_photos
import photocreateai.composeapp.generated.resources.analyzing_photos
import photocreateai.composeapp.generated.resources.creating_model_hint
import photocreateai.composeapp.generated.resources.delete
import photocreateai.composeapp.generated.resources.delete_unsuitable_photos
import photocreateai.composeapp.generated.resources.generate_photo
import photocreateai.composeapp.generated.resources.select_photos_popup_message
import photocreateai.composeapp.generated.resources.train_ai_model
import photocreateai.composeapp.generated.resources.training_ai_model
import photocreateai.composeapp.generated.resources.upload_guidelines_message
import photocreateai.composeapp.generated.resources.upload_more_photos


@Preview
@Composable
fun UploadScreen(
    viewModel: UploadViewModel = viewModel { UploadViewModel() },
    openGenerateTab: () -> Unit,
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

        if (state.isLoadingPhotos) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else if (state.photos.isNullOrEmpty()) {
            Placeholder(modifier = Modifier.align(Alignment.Center))
        } else {
            LaunchedEffect(state.scrollToTop) {
                if (state.scrollToTop && state.listState.firstVisibleItemIndex > 1) {
                    state.listState.animateScrollToItem(0)
                }
                viewModel.resetScrollToTop()
            }
            val hideDeletePhotoButton = state.isLoadingTraining || state.trainingStatus != null
            Photos(
                photos = state.photos,
                listState = state.listState,
                selectMode = state.selectMode,
                onSelect = viewModel::selectPhoto,
                hideDeletePhotoButton = hideDeletePhotoButton,
                onDelete = viewModel::deletePhoto,
            )
        }

        val buttonsBottomPadding = 94.dp
        if (!state.isLoadingPhotos && state.photos != null) {
            if (!state.isLoadingTraining && state.photos.size >= 10) {
                val shouldAnalyzePhotos = state.photos.any { it.analysisStatus == null }
                if (shouldAnalyzePhotos) {
                    AnalyzePhotosFab(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = buttonsBottomPadding),
                        analyzingPhotos = state.analyzingPhotos,
                        onClick = viewModel::analyzePhotos,
                    )
                } else {
                    CreateModelFab(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = buttonsBottomPadding),
                        trainingStatus = state.trainingStatus,
                        createModel = { viewModel.toggleTrainAiModelPopup(true) },
                        onCreatingModelClick = viewModel::onCreatingModelClick,
                        generatePhotos = openGenerateTab,
                    )
                }
                if (state.selectMode) {
                    FloatingActionButton(
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .padding(bottom = buttonsBottomPadding, end = 24.dp),
                        onClick = { viewModel.toggleSelectMode(false) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = Icons.Default.Close.name,
                            )
                            Text(
                                modifier = Modifier.animateContentSize(),
                                text = state.photos.count { it.selected }.toString(),
                                fontSize = 16.sp,
                            )
                        }
                    }
                } else {
                    SmallFloatingActionButton(
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .padding(bottom = buttonsBottomPadding, end = 24.dp),
                        onClick = onAddPhotoClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = Icons.Default.AddAPhoto.name,
                        )
                    }
                }

                val hasBadPhotos = state.photos.any { it.analysisStatus == AnalysisStatus.DECLINED }
                if (hasBadPhotos) {
                    SmallFloatingActionButton(
                        modifier = Modifier.align(Alignment.BottomStart)
                            .padding(bottom = buttonsBottomPadding, start = 24.dp),
                        onClick = { viewModel.toggleDeleteUnsuitablePhotosPopup(true) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = Icons.Default.CleaningServices.name,
                        )
                    }
                }
            } else {
                AddPhotosFab(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = buttonsBottomPadding),
                    uploadProgress = state.uploadProgress,
                    onClick = onAddPhotoClick,
                )
                SmallFloatingActionButton(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(bottom = buttonsBottomPadding, end = 24.dp),
                    onClick = { viewModel.toggleTrainAiModelPopup(true) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = Icons.Default.Memory.name,
                    )
                }
            }
        }

        if (state.showUploadMorePhotosPopup) {
            InfoPopup(stringResource(Res.string.upload_more_photos)) {
                viewModel.hideUploadMorePhotosPopup()
            }
        }

        if (state.showTrainingAiModelPopup) {
            InfoPopup(stringResource(Res.string.creating_model_hint)) {
                viewModel.hideCreatingModelClick()
            }
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }


        if (state.showTrainAiModelPopup) {
            TrainAiModelPopup {
                viewModel.toggleTrainAiModelPopup(false)
                viewModel.createModel()
            }
        }

        if (state.showSelectPhotosPopup) {
            InfoPopup(stringResource(Res.string.select_photos_popup_message)) {
                viewModel.toggleShowSelectPhotosPopup(false)
            }
        }

        if (state.deleteUnsuitablePhotosPopup) {
            ConfirmationPopup(
                icon = Icons.Default.Delete,
                message = stringResource(Res.string.delete_unsuitable_photos),
                confirmButton = stringResource(Res.string.delete),
                onConfirm = viewModel::deleteUnsuitablePhotos,
                onDismiss = { viewModel.toggleDeleteUnsuitablePhotosPopup(false) },
            )
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
    uploadProgress: Int,
    onClick: () -> Unit
) {
    val isLoading = uploadProgress in 1 until 100
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = { if (!isLoading) onClick() },
    ) {
        if (uploadProgress in 1 until 100) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uploadProgress}%",
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = { uploadProgress / 100f })
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = stringResource(Res.string.add_your_photos),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.add_your_photos),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun AnalyzePhotosFab(
    modifier: Modifier = Modifier,
    analyzingPhotos: Boolean,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        if (analyzingPhotos) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.analyzing_photos),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ImageSearch,
                    contentDescription = Icons.Default.ImageSearch.name,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.analyze_photos),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun CreateModelFab(
    modifier: Modifier = Modifier,
    trainingStatus: TrainingStatus?,
    createModel: () -> Unit,
    onCreatingModelClick: () -> Unit,
    generatePhotos: () -> Unit,
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = {
            when (trainingStatus) {
                TrainingStatus.SUCCEEDED -> generatePhotos()
                TrainingStatus.PROCESSING -> onCreatingModelClick()
                null -> createModel()
            }
        },
    ) {
        when (trainingStatus) {

            TrainingStatus.SUCCEEDED -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = stringResource(Res.string.train_ai_model),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(Res.string.generate_photo),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                    )
                }
            }

            TrainingStatus.PROCESSING -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(Res.string.training_ai_model),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                    )
                }
            }

            null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = stringResource(Res.string.train_ai_model),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(Res.string.train_ai_model),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                    )
                }
            }

        }
    }
}


@Composable
private fun Photos(
    photos: List<UploadUiState.Photo>,
    listState: LazyStaggeredGridState,
    selectMode: Boolean,
    onSelect: (UploadUiState.Photo) -> Unit,
    hideDeletePhotoButton: Boolean,
    onDelete: (UploadUiState.Photo) -> Unit,
) {
    var showAnalysis by remember { mutableStateOf(true) }

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
                selectMode = selectMode,
                onSelect = onSelect,
                hideDeletePhotoButton = hideDeletePhotoButton,
                onDelete = onDelete,
                showAnalysis = showAnalysis,
                onToggleShowAnalysis = { showAnalysis = !showAnalysis },
            )
        }
    }
}

@Composable
private fun Photo(
    modifier: Modifier,
    photo: UploadUiState.Photo,
    selectMode: Boolean,
    onSelect: (UploadUiState.Photo) -> Unit,
    hideDeletePhotoButton: Boolean,
    onDelete: (UploadUiState.Photo) -> Unit,
    showAnalysis: Boolean,
    onToggleShowAnalysis: () -> Unit,
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

    Box(
        modifier = modifier.fillMaxWidth().then(
            if (selectMode) {
                Modifier.background(Color.Black)
            } else {
                Modifier
            }
        )
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth().clickable {
                if (selectMode) onSelect(photo)
            }.alpha(if (selectMode && !photo.selected) 0.5f else 1f),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photo.url)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            onSuccess = { loading = false },
            onError = {
                Logger.e("error loading image ${photo.url}", it.result.throwable)
                error = it.result.throwable
                loading = false
            },
            contentDescription = "photo",
        )

        if (selectMode) {
            IconButton(
                onClick = { onSelect(photo) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Crossfade(targetState = photo.selected) { selected ->
                    val icon =
                        if (selected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank
                    Icon(
                        imageVector = icon,
                        contentDescription = icon.name,
                        tint = Color.White,
                    )
                }
            }
        } else if (!hideDeletePhotoButton && !loading) {
            IconButton(
                onClick = { onDelete(photo) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Icons.Default.Close.name,
                    tint = Color.White,
                )
            }
        }


        if (!loading && photo.analysisStatus != null) {
            IconButton(
                onClick = onToggleShowAnalysis,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                val image = when (photo.analysisStatus) {
                    AnalysisStatus.PROCESSING -> Icons.Default.Sync
                    AnalysisStatus.APPROVED -> Icons.Default.ThumbUp
                    AnalysisStatus.DECLINED -> Icons.Default.ThumbDown
                }
                Icon(
                    imageVector = image,
                    contentDescription = image.name,
                    tint = Color.White,
                )
            }
        }

        if (!loading && showAnalysis && !photo.analysis.isNullOrEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 64.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                text = photo.analysis,
                color = Color.White,
                fontSize = 16.sp,
            )
        }
    }
}