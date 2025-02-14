package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import ai.create.photo.ui.compose.ConfirmationPopup
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.training.TrainAiModelPopup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import photocreateai.composeapp.generated.resources.delete_some_photos
import photocreateai.composeapp.generated.resources.delete_unsuitable_photos
import photocreateai.composeapp.generated.resources.generate_photo
import photocreateai.composeapp.generated.resources.train_ai_model
import photocreateai.composeapp.generated.resources.training_ai_model
import photocreateai.composeapp.generated.resources.upload_guidelines_footer
import photocreateai.composeapp.generated.resources.upload_guidelines_header
import photocreateai.composeapp.generated.resources.upload_guidelines_message_1
import photocreateai.composeapp.generated.resources.upload_guidelines_message_2
import photocreateai.composeapp.generated.resources.upload_guidelines_message_3
import photocreateai.composeapp.generated.resources.upload_guidelines_message_4
import photocreateai.composeapp.generated.resources.upload_guidelines_step_1
import photocreateai.composeapp.generated.resources.upload_guidelines_step_2
import photocreateai.composeapp.generated.resources.upload_guidelines_step_3
import photocreateai.composeapp.generated.resources.upload_guidelines_step_4
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
            Placeholder(modifier = Modifier.align(Alignment.TopCenter))
        } else {
            LaunchedEffect(state.scrollToTop) {
                if (state.scrollToTop && state.listState.firstVisibleItemIndex > 1) {
                    state.listState.animateScrollToItem(0)
                }
                viewModel.resetScrollToTop()
            }
            val hideDeletePhotoButton = state.trainingStatus == TrainingStatus.PROCESSING
            Photos(
                photos = state.photos,
                listState = state.listState,
                showAnalysisForAll = state.showAnalysisForAll,
                hideDeletePhotoButton = hideDeletePhotoButton,
                onDelete = viewModel::deletePhoto,
            )
        }

        val buttonsBottomPadding = 94.dp
        if (!state.isLoadingPhotos && state.photos != null) {
            val isUploading = state.uploadProgress in 1 until 100
            if (state.photos.size >= 10 && !isUploading) {
                val shouldAnalyzePhotos = state.photos.any { it.analysisStatus == null }
                if (shouldAnalyzePhotos) {
                    AnalyzePhotosFab(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = buttonsBottomPadding),
                        analyzingPhotos = state.analyzingPhotos,
                        totalPhotos = state.photos.size,
                        onClick = viewModel::analyzePhotos,
                    )
                } else {
                    TrainModelFab(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = buttonsBottomPadding),
                        trainingStatus = state.trainingStatus,
                        createModel = { viewModel.toggleTrainAiModelPopup(true) },
                        onCreatingModelClick = viewModel::onCreatingModelClick,
                        generatePhotos = openGenerateTab,
                    )
                }
                if (state.photos.size < 20) {
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
                    uploaded = state.photos.size,
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

        if (state.showDeleteSomePhotosPopup) {
            InfoPopup(stringResource(Res.string.delete_some_photos)) {
                viewModel.hideDeleteSomePhotosPopup()
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
            TrainAiModelPopup(
                photosCount = state.photos?.size ?: 0,
                onDismiss = { viewModel.toggleTrainAiModelPopup(false) },
                onConfirm = { steps ->
                    viewModel.toggleTrainAiModelPopup(false)
                    viewModel.trainAiModel(steps)
                },
            )
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
private fun Placeholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.upload_guidelines_header),
            fontSize = 18.sp,
        )

        UploadGuidelineStep(
            step = stringResource(Res.string.upload_guidelines_step_1),
            message = stringResource(Res.string.upload_guidelines_message_1)
        )
        UploadGuidelineStep(
            step = stringResource(Res.string.upload_guidelines_step_2),
            message = stringResource(Res.string.upload_guidelines_message_2)
        )
        UploadGuidelineStep(
            step = stringResource(Res.string.upload_guidelines_step_3),
            message = stringResource(Res.string.upload_guidelines_message_3)
        )
        UploadGuidelineStep(
            step = stringResource(Res.string.upload_guidelines_step_4),
            message = stringResource(Res.string.upload_guidelines_message_4)
        )

        Text(
            text = stringResource(Res.string.upload_guidelines_footer),
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun UploadGuidelineStep(step: String, message: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = step,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
        Text(
            text = message,
            fontSize = 14.sp,
        )
    }
}


@Composable
private fun AddPhotosFab(
    modifier: Modifier = Modifier,
    uploadProgress: Int,
    uploaded: Int,
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
                    contentDescription = Icons.Default.AddAPhoto.name,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.add_your_photos, uploaded),
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
    analyzingPhotos: Int,
    totalPhotos: Int,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        if (analyzingPhotos > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(
                        Res.string.analyzing_photos,
                        analyzingPhotos,
                        totalPhotos
                    ),
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
private fun TrainModelFab(
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
    showAnalysisForAll: Boolean,
    hideDeletePhotoButton: Boolean,
    onDelete: (UploadUiState.Photo) -> Unit,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Photo(
                    photo = photos[item],
                    showAnalysisForAll = showAnalysisForAll,
                    hideDeletePhotoButton = hideDeletePhotoButton,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun Photo(
    photo: UploadUiState.Photo,
    showAnalysisForAll: Boolean,
    hideDeletePhotoButton: Boolean,
    onDelete: (UploadUiState.Photo) -> Unit,
) {
    var loaded by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<Throwable?>(null) }
    error?.let {
        ErrorMessagePlaceHolderSmall(it)
    }

    var showAnalysis by remember { mutableStateOf(false) }
    LaunchedEffect(showAnalysisForAll) {
        showAnalysis = showAnalysisForAll
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photo.url)
                .crossfade(true)
                .build(),
            contentDescription = photo.analysis,
            contentScale = ContentScale.FillWidth,
            onSuccess = { loaded = true },
            onError = {
                Logger.e("error loading image ${photo.url}", it.result.throwable)
                error = it.result.throwable
            },
        )

        if (loaded && !hideDeletePhotoButton) {
            IconButton(
                onClick = { onDelete(photo) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Icons.Default.Close.name,
                )
            }
        }


        if (loaded && photo.analysisStatus != null) {
            IconButton(
                onClick = { showAnalysis = !showAnalysis },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                val image = when (photo.analysisStatus) {
                    AnalysisStatus.PROCESSING -> Icons.Default.Sync
                    AnalysisStatus.APPROVED -> Icons.Default.ThumbUp
                    AnalysisStatus.DECLINED -> Icons.Default.ThumbDown
                }
                Icon(
                    imageVector = image,
                    contentDescription = image.name,
                )
            }
        }

        if (loaded && showAnalysis && !photo.analysis.isNullOrEmpty()) {
            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 64.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                text = photo.analysis,
                fontSize = 16.sp,
            )
        }
    }
}