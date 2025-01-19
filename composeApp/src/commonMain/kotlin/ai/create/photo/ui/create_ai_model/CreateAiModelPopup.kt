package ai.create.photo.ui.create_ai_model

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.create_ai_model


@Preview
@Composable
fun CreateAiModelPopup(
    viewModel: CreateAiModelViewModel = viewModel { CreateAiModelViewModel() },
    onDismiss: () -> Unit,
) {
    val state = viewModel.uiState

    AlertDialog(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(0.85f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = { stringResource(Res.string.create_ai_model) },
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
                }
            }
        },
        confirmButton = {}
    )

}