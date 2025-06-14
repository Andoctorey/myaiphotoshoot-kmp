package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import ai.create.photo.ui.compose.ConfirmationPopup
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.compose.TopUpErrorPopup
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.add_your_photos
import photocreateai.composeapp.generated.resources.add_your_photos_progress
import photocreateai.composeapp.generated.resources.analyze_photos
import photocreateai.composeapp.generated.resources.analyzing_photos
import photocreateai.composeapp.generated.resources.continue_
import photocreateai.composeapp.generated.resources.creating_model_hint
import photocreateai.composeapp.generated.resources.delete
import photocreateai.composeapp.generated.resources.delete_some_photos
import photocreateai.composeapp.generated.resources.delete_unsuitable_photos
import photocreateai.composeapp.generated.resources.generate_photo
import photocreateai.composeapp.generated.resources.thank_you_for_purchase
import photocreateai.composeapp.generated.resources.topping_up
import photocreateai.composeapp.generated.resources.train_ai_model
import photocreateai.composeapp.generated.resources.training_ai_model
import photocreateai.composeapp.generated.resources.upload_guidelines
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
                    Logger.i("scrolling to top")
                }
                viewModel.resetScrollToTop()
            }
            LaunchedEffect(state.scrollToPosition) {
                if (state.scrollToPosition != null) {
                    if (state.scrollToPosition != 0) {
                        Logger.i("scrolling to position: ${state.scrollToPosition}")
                        state.listState.animateScrollToItem(state.scrollToPosition)
                    }
                    viewModel.resetScrollToPosition()
                }
            }
            val hideDeletePhotoButton = state.trainingStatus == TrainingStatus.PROCESSING
            Photos(
                photos = state.photos,
                listState = state.listState,
                hideDeletePhotoButton = hideDeletePhotoButton,
                onDelete = viewModel::deletePhoto,
            )
        }

        val buttonsBottomPadding = 94.dp
        if (!state.isLoadingPhotos && state.photos != null) {
            val isUploading = state.uploadProgress in 1 until 100
            val shouldAnalyzePhotos =
                state.photos.any { it.analysisStatus == null || it.analysisStatus == AnalysisStatus.PROCESSING }
            if (!isUploading && (shouldAnalyzePhotos || state.photos.size >= 10)) {
                if (shouldAnalyzePhotos) {
                    AnalyzePhotosFab(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = buttonsBottomPadding).safeDrawingPadding(),
                        analyzingPhotos = state.analyzingPhotos,
                        totalPhotos = state.photos.size,
                        onClick = viewModel::analyzePhotos,
                    )
                } else {
                    TrainModelFab(
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = buttonsBottomPadding).safeDrawingPadding(),
                        trainingStatus = state.trainingStatus,
                        trainingTimeLeft = state.trainingTimeLeft,
                        createModel = { viewModel.checkBadPhotosAndToggleTrainAiModelPopup(true) },
                        onCreatingModelClick = viewModel::onCreatingModelClick,
                        generatePhotos = openGenerateTab,
                        isToppingUp = state.toppingUp,
                    )
                }
                if (state.photos.size < 20 && state.trainingStatus != TrainingStatus.PROCESSING) {
                    SmallFloatingActionButton(
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .padding(bottom = buttonsBottomPadding, end = 24.dp)
                            .safeDrawingPadding(),
                        onClick = { launcher.launch() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = Icons.Default.AddAPhoto.name,
                        )
                    }
                }

                val hasBadPhotos = state.photos.any { it.analysisStatus == AnalysisStatus.DECLINED }
                if (hasBadPhotos && state.trainingStatus != TrainingStatus.PROCESSING) {
                    SmallFloatingActionButton(
                        modifier = Modifier.align(Alignment.BottomStart)
                            .padding(bottom = buttonsBottomPadding, start = 24.dp)
                            .safeDrawingPadding(),
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
                        .padding(bottom = buttonsBottomPadding).safeDrawingPadding(),
                    uploadProgress = state.uploadProgress,
                    uploaded = state.photos.size,
                    onClick = { launcher.launch() },
                )
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

        if (state.topUpErrorPopup != null) {
            TopUpErrorPopup(
                state.topUpErrorPopup,
                onDismiss = { viewModel.hideErrorPopup() },
                onTopUp = {
                    viewModel.hideErrorPopup()
                    viewModel.topUp()
                })
        }

        if (state.deleteUnsuitablePhotosPopup) {
            ConfirmationPopup(
                icon = Icons.Default.Delete,
                message = stringResource(Res.string.delete_unsuitable_photos),
                confirmButton = stringResource(Res.string.delete),
                dismissButton = stringResource(Res.string.continue_),
                onConfirm = viewModel::deleteUnsuitablePhotos,
                onDismiss = {
                    viewModel.toggleDeleteUnsuitablePhotosPopup(false)
                    viewModel.trainAiModel()
                },
            )
        }

        if (state.showBalanceUpdatedPopup) {
            InfoPopup(stringResource(Res.string.thank_you_for_purchase)) {
                viewModel.hideBalanceUpdatedPopup()
            }
        }
    }
}

@Composable
private fun Placeholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .widthIn(max = 600.dp)
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 160.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = stringResource(Res.string.upload_guidelines),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )

        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data("https://myaiphotoshoot.b-cdn.net/upload_placeholder.png")
                .build(),
            contentDescription = stringResource(Res.string.upload_guidelines),
            contentScale = ContentScale.FillWidth,
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
                    text = stringResource(Res.string.add_your_photos_progress, uploaded),
                    textAlign = TextAlign.Center,
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
                        totalPhotos - analyzingPhotos,
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
    trainingTimeLeft: Long,
    createModel: () -> Unit,
    onCreatingModelClick: () -> Unit,
    generatePhotos: () -> Unit,
    isToppingUp: Boolean,
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = {
            if (isToppingUp) return@ExtendedFloatingActionButton
            when (trainingStatus) {
                TrainingStatus.SUCCEEDED -> generatePhotos()
                TrainingStatus.PROCESSING -> onCreatingModelClick()
                TrainingStatus.FAILED, null -> createModel()
            }
        },
    ) {
        if (isToppingUp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.topping_up),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
            }
        } else {
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
                            text = stringResource(Res.string.training_ai_model) + " " +
                                    if (trainingTimeLeft > 0) {
                                        "(${(trainingTimeLeft / 1000).toInt()}s)"
                                    } else {
                                        ""
                                    },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                        )
                    }
                }

                TrainingStatus.FAILED, null -> {
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
}

@Composable
private fun Photos(
    photos: List<UploadUiState.Photo>,
    listState: LazyStaggeredGridState,
    hideDeletePhotoButton: Boolean,
    onDelete: (UploadUiState.Photo) -> Unit,
) {

    val optimizedVersion = remember {
        platform().platform in listOf(
            Platforms.WEB_MOBILE,
            Platforms.WEB_DESKTOP,
        )
    }

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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(if (!optimizedVersion || item < 3) Modifier.animateItem() else Modifier),
            ) {
                Photo(
                    photo = photos[item],
                    optimizedVersion = optimizedVersion,
                    hideDeletePhotoButton = hideDeletePhotoButton,
                    onDelete = onDelete,
                )
            }
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}

@Composable
private fun Photo(
    photo: UploadUiState.Photo,
    optimizedVersion: Boolean,
    hideDeletePhotoButton: Boolean,
    onDelete: (UploadUiState.Photo) -> Unit,
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

    var showAnalysis by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxWidth().then(if (loaded) Modifier else Modifier.aspectRatio(1f))
    ) {
        if (showImage) {
            AsyncImage(
                modifier = Modifier.fillMaxWidth(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photo.url)
                    .crossfade(!optimizedVersion)
                    .build(),
                contentDescription = photo.analysis,
                contentScale = ContentScale.FillWidth,
                onSuccess = { loaded = true },
                onError = {
                    Logger.e("error loading image ${photo.url}", it.result.throwable)
                    error = it.result.throwable
                },
            )
        }

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