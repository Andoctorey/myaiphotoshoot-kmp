package ai.create.photo.ui.training

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.InfoPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
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
import photocreateai.composeapp.generated.resources.minutes
import photocreateai.composeapp.generated.resources.photos_required
import photocreateai.composeapp.generated.resources.steps
import photocreateai.composeapp.generated.resources.train_ai_model


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun TrainAiModelPopup(
    viewModel: TrainAiModelViewModel = viewModel { TrainAiModelViewModel() },
    photosCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val state = viewModel.uiState

    AlertDialog(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth(0.85f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.train_ai_model),
            )
        },
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
                    StepsSelector { steps ->
                        if (photosCount < 10) {
                            viewModel.showPhotosRequiredPopup(10, photosCount)
                        } else {
                            onConfirm(steps)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.cancel),
                    fontSize = 16.sp,
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
private fun StepsSelector(onStepsSelected: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Model(
            steps = 1000,
            minutes = 30,
            cost = "$3.99",
            onClick = { onStepsSelected(1000) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Model(
            steps = 1500,
            minutes = 45,
            cost = "$7.99",
            onClick = { onStepsSelected(1500) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Model(
            steps = 2000,
            minutes = 60,
            cost = "$7.99",
            onClick = { onStepsSelected(2000) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.cost_per_photo),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun Model(steps: Int, minutes: Int, cost: String, onClick: () -> Unit = {}) {
    OutlinedButton(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "$steps ${stringResource(Res.string.steps)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "$minutes ${stringResource(Res.string.minutes)}",
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = cost,
                fontSize = 32.sp
            )
        }
    }
}