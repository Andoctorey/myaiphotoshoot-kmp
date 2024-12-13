package ai.create.photo.ui.create

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.compose.getFriendlyError
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.add_your_photos
import photocreateai.composeapp.generated.resources.upload_guidelines_message


@Preview
@Composable
fun CreateScreen(
    viewModel: CreateViewModel = viewModel { CreateViewModel() },
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

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else if (state.photos.isNullOrEmpty()) {
            Placeholder(modifier = Modifier.align(Alignment.Center))
        } else {
            Photos(state.photos, state.listState)
        }

        AddPhotosFab(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            uploadProgress = state.uploadProgress,
            errorMessage = state.uploadError,
        ) {
            launcher.launch()
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
    modifier: Modifier, uploadProgress: Int, errorMessage: Throwable?, onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        val isLoading = uploadProgress in 1 until 100 || errorMessage != null
        if (errorMessage != null) {
            Text(
                fontSize = 12.sp,
                text = errorMessage.getFriendlyError(),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
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
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "error",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(Res.string.add_your_photos),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}


@Composable
private fun Photos(photos: List<CreateUiState.Photo>, listState: LazyStaggeredGridState) {
    LazyVerticalStaggeredGrid(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        columns = StaggeredGridCells.Adaptive(minSize = 540.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        }

        items(photos.size, key = { photos[it].id }) { item ->
            Photo(photos[item])
        }
    }
}

@Composable
private fun Photo(photo: CreateUiState.Photo) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Throwable?>(null) }

    if (error != null) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            ErrorMessagePlaceHolder(error!!)
        }
    } else if (loading) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            LoadingPlaceholder()
        }
    }

    AsyncImage(
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
}