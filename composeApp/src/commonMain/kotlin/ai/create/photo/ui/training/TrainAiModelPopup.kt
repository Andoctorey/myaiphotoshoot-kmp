package ai.create.photo.ui.training

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.cancel
import photocreateai.composeapp.generated.resources.cost_per_photo
import photocreateai.composeapp.generated.resources.minimum_photos_required
import photocreateai.composeapp.generated.resources.minutes
import photocreateai.composeapp.generated.resources.photos_required
import photocreateai.composeapp.generated.resources.select_training_steps
import photocreateai.composeapp.generated.resources.steps
import photocreateai.composeapp.generated.resources.train_ai_model
import kotlin.math.round


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun TrainAiModelPopup(
    viewModel: TrainAiModelViewModel = viewModel { TrainAiModelViewModel() },
    photosCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val state = viewModel.uiState

    AlertDialog(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth(0.85f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(20.dp))
                    LoadingPlaceholder()
                } else if (state.loadingError != null) {
                    ErrorMessagePlaceHolder(state.loadingError)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    ) {
                        TrainingSteps(
                            steps = state.trainingSteps,
                            onStepsChanged = { viewModel.updateTrainingSteps(it) })
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.cancel),
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val photosRequired = state.trainingSteps / 100
                if (photosCount < photosRequired) {
                    viewModel.showPhotosRequiredPopup(photosRequired, photosCount)
                } else {
                    onConfirm()
                }
            }) {
                Text(
                    text = stringResource(Res.string.train_ai_model),
                    fontSize = 16.sp
                )
            }
        }
    )
    if (state.showPhotosRequiredPopup) {
        InfoPopup(
            stringResource(
                Res.string.photos_required,
                state.photosTaken,
                (state.photosRequired - state.photosTaken)
            )
        ) {
            viewModel.hidePhotosRequiredPopup()
        }
    }
}

@Composable
private fun TrainingSteps(steps: Int, onStepsChanged: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.animateContentSize(),
            text = stringResource(Res.string.select_training_steps),
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.animateContentSize(),
            text = "$${round(steps / 500f * 2.49 * 100f) / 100f}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            modifier = Modifier.animateContentSize(),
            text = stringResource(Res.string.cost_per_photo),
            fontSize = 14.sp,
        )
        Slider(
            value = steps.toFloat(),
            onValueChange = { onStepsChanged(it.toInt()) },
            valueRange = 500f..2000f,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.animateContentSize(),
            text = steps.toString() + " " + stringResource(Res.string.steps),
            fontSize = 14.sp,
        )
        Text(
            modifier = Modifier.animateContentSize(),
            text = (steps / 2000f * 60).toInt()
                .toString() + " " + stringResource(Res.string.minutes),
            fontSize = 14.sp,
        )
        Text(
            modifier = Modifier.animateContentSize(),
            text = stringResource(Res.string.minimum_photos_required, steps / 100),
            fontSize = 14.sp,
        )
    }
}