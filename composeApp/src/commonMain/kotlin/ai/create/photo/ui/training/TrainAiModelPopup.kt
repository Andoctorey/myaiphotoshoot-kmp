package ai.create.photo.ui.train_ai_model

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import photocreateai.composeapp.generated.resources.cost
import photocreateai.composeapp.generated.resources.minutes
import photocreateai.composeapp.generated.resources.select_training_steps
import photocreateai.composeapp.generated.resources.train_ai_model
import photocreateai.composeapp.generated.resources.training_steps
import kotlin.math.round


@Preview
@Composable
fun TrainAiModelPopup(
    viewModel: TrainAiModelViewModel = viewModel { TrainAiModelViewModel() },
    onDismiss: () -> Unit,
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
//        Text(stringResource(Res.string.train_ai_model_step_1))
//        Text(stringResource(Res.string.train_ai_model_step_2))
//        Text(stringResource(Res.string.train_ai_model_step_3))
//        Text(stringResource(Res.string.train_ai_model_step_4))
//        Text(stringResource(Res.string.train_ai_model_step_5))
//        Text(stringResource(Res.string.train_ai_model_step_final))
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
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.train_ai_model),
                    fontSize = 16.sp
                )
            }
        }
    )

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
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = steps.toFloat(),
            onValueChange = { onStepsChanged(it.toInt()) },
            valueRange = 500f..2000f,
        )
        val minutes = (steps / 1000f * 60).toInt()
        Text(
            modifier = Modifier.animateContentSize(),
            text = stringResource(Res.string.training_steps) + ": $steps. " +
                    stringResource(Res.string.minutes) + ": $minutes.",
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        val cost = round(steps / 500f * 4.99 * 100f) / 100f
        Text(
            modifier = Modifier.animateContentSize(),
            text = stringResource(Res.string.cost, cost),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}