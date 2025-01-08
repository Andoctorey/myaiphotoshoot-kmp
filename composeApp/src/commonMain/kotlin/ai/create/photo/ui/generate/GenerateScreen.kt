package ai.create.photo.ui.generate

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.create_ai_model
import photocreateai.composeapp.generated.resources.enhance_photo_accuracy
import photocreateai.composeapp.generated.resources.generate_photo
import photocreateai.composeapp.generated.resources.photo_prompt


@Preview
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel = viewModel { GenerateViewModel() },
    onGenerate: (String) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val state = viewModel.uiState

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(20.dp))
            LoadingPlaceholder()
        } else if (state.loadingError != null) {
            ErrorMessagePlaceHolder(state.loadingError)
        } else {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                if (state.aiVisionPrompt.isNotEmpty()) {
                    AiVisionPrompt(
                        prompt = state.aiVisionPrompt,
                        onPromptChanged = viewModel::onAiVisionPromptChanged,
                        expanded = state.expanded,
                        onExpand = viewModel::onExpand,
                        isLoadingAiVisionPrompt = state.isLoadingAiVisionPrompt,
                        onRefreshAiVisionPrompt = viewModel::onRefreshAiVisionPrompt,
                    )
                }

                PhotoPrompt(prompt = state.userPrompt) {
                    viewModel.onUserPromptChanged(it)
                }
            }

            GenerateFab(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            ) {
                viewModel.prepareToGenerate(onGenerate)
                onGenerate(state.userPrompt)
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
fun AiVisionPrompt(
    prompt: String, onPromptChanged: (String) -> Unit,
    expanded: Boolean = false, onExpand: () -> Unit,
    isLoadingAiVisionPrompt: Boolean = false, onRefreshAiVisionPrompt: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(24.dp)
            .animateContentSize(),
        value = prompt,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoadingAiVisionPrompt) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        modifier = Modifier.clickable { onRefreshAiVisionPrompt() },
                        imageVector = Icons.Default.Refresh,
                        contentDescription = Icons.Default.Refresh.name,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                val icon =
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                Icon(
                    modifier = Modifier.clickable { onExpand() },
                    imageVector = icon,
                    contentDescription = icon.name,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        onValueChange = onPromptChanged,
        singleLine = !expanded,
        label = { Text(text = stringResource(Res.string.enhance_photo_accuracy)) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
        )
    )
}

@Composable
fun PhotoPrompt(prompt: String, onPromptChanged: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(24.dp),
        value = prompt,
        onValueChange = onPromptChanged,
        label = { Text(text = stringResource(Res.string.photo_prompt)) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
        )
    )
}

@Composable
private fun GenerateFab(modifier: Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = stringResource(Res.string.create_ai_model),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(Res.string.generate_photo),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}