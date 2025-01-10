package ai.create.photo.ui.generate

import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorPopup
import ai.create.photo.ui.compose.LoadingPlaceholder
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.ai_model
import photocreateai.composeapp.generated.resources.create_ai_model
import photocreateai.composeapp.generated.resources.enhance_photo_accuracy
import photocreateai.composeapp.generated.resources.enhance_prompt
import photocreateai.composeapp.generated.resources.photo_prompt
import photocreateai.composeapp.generated.resources.photos_to_generate
import photocreateai.composeapp.generated.resources.surprise_me


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel = viewModel { GenerateViewModel() },
    createTraining: () -> Unit,
    onGenerate: (String, String, Int) -> Unit,
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

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxSize()
                    .animateContentSize().verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Card(
                    border = if (state.showSettings) BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.onSurface
                    ) else null,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (state.trainings != null) {
                            Box(modifier = Modifier.fillMaxWidth().systemBarsPadding()) {
                                Trainings(
                                    modifier = Modifier.align(Alignment.Center),
                                    trainings = state.trainings,
                                    selectedTraining = state.training,
                                    selectTraining = viewModel::selectTraining,
                                    createTraining = createTraining,
                                )
                                IconButton(
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    onClick = viewModel::toggleSettings,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = Icons.Default.Settings.name,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        Crossfade(targetState = state.showSettings) { showSettings ->
                            if (showSettings) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (state.aiVisionPrompt.isNotEmpty()) {
                                        AiVisionPrompt(
                                            prompt = state.aiVisionPrompt,
                                            onPromptChanged = viewModel::onAiVisionPromptChanged,
                                            expanded = state.aiVisionPromptExpanded,
                                            onExpand = viewModel::onExpand,
                                            isLoadingAiVisionPrompt = state.isLoadingAiVisionPrompt,
                                            onRefreshAiVisionPrompt = viewModel::onRefreshAiVisionPrompt,
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    PhotosToGenerate(state.photosToGenerateX100) {
                                        viewModel.onPhotosToGenerateChanged(it)
                                    }
                                }
                            }
                        }
                    }
                }


                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                ) {
                    EnhancePromptButton(state.isEnhancingPrompt, viewModel::enhancePrompt)

                    SurpriseMeButton(state.isLoadingSurpriseMe, viewModel::surpriseMe)
                }


                var previousText by remember { mutableStateOf("") }
                LaunchedEffect(state.userPrompt) {
                    if (state.userPrompt.count { it == '\n' } > previousText.count { it == '\n' }) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                    previousText = state.userPrompt
                }

                PhotoPrompt(prompt = state.userPrompt,
                    onPromptChanged = viewModel::onUserPromptChanged,
                    onGenerate = { viewModel.prepareToGenerate(onGenerate) })
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
private fun AiVisionPrompt(
    prompt: String, onPromptChanged: (String) -> Unit,
    expanded: Boolean = false, onExpand: () -> Unit,
    isLoadingAiVisionPrompt: Boolean = false, onRefreshAiVisionPrompt: () -> Unit,
) {
    val isExpandedPrompt = prompt.length > 150
    OutlinedTextField(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 24.dp)
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
                if (isExpandedPrompt) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val icon =
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                    Icon(
                        modifier = Modifier.clickable { onExpand() },
                        imageVector = icon,
                        contentDescription = icon.name,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        },
        onValueChange = onPromptChanged,
        singleLine = isExpandedPrompt && !expanded,
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
private fun PhotoPrompt(
    prompt: String,
    onPromptChanged: (String) -> Unit,
    onGenerate: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val onDone = {
        focusManager.clearFocus()
        onGenerate()
    }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
        value = prompt,
        onValueChange = onPromptChanged,
        label = { Text(text = stringResource(Res.string.photo_prompt)) },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = true,
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Crossfade(targetState = prompt.isNotEmpty()) {
                    if (it) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                onPromptChanged("")
                            },
                        ) {
                            Icon(
                                modifier = Modifier.clickable { onPromptChanged("") },
                                imageVector = Icons.Default.Close,
                                contentDescription = Icons.Default.Close.name,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                IconButton(onClick = { onDone() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = Icons.AutoMirrored.Filled.Send.name,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
    )
}

@Composable
private fun EnhancePromptButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        TextButton(
            modifier = Modifier.alpha(if (isLoading) 0f else 1f),
            onClick = onClick
        ) {
            Text(
                text = stringResource(Res.string.enhance_prompt),
                fontSize = 16.sp,
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(horizontal = 8.dp).size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun SurpriseMeButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        TextButton(
            modifier = Modifier.alpha(if (isLoading) 0f else 1f),
            onClick = onClick,
        ) {
            Text(
                text = stringResource(Res.string.surprise_me),
                fontSize = 16.sp,
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(horizontal = 8.dp).size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Trainings(
    modifier: Modifier,
    trainings: List<GenerateUiState.Training?>,
    selectedTraining: GenerateUiState.Training?,
    selectTraining: (GenerateUiState.Training) -> Unit,
    createTraining: () -> Unit
) {
    val options = trainings.toMutableList()
    options.add(null)
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(selectedTraining) }
    val aiModelString = stringResource(Res.string.ai_model)
    val createAiModelString = stringResource(Res.string.create_ai_model)
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextButton(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
            onClick = { },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedOption == null) createAiModelString
                    else "$aiModelString ${options.size - options.indexOf(selectedOption) - 1}",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "error",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (option == null) createAiModelString
                            else "$aiModelString ${options.size - index - 1}",
                        )
                    },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        if (selectedOption == null) createTraining()
                        else selectTraining(selectedOption!!)
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotosToGenerate(photosToGenerate: Int, onPhotosToGenerateChanged: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.animateContentSize(),
            text = stringResource(Res.string.photos_to_generate) + ": ${photosToGenerate / 100}",
            fontSize = 14.sp,
        )
        Slider(
            value = photosToGenerate.toFloat(),
            onValueChange = { onPhotosToGenerateChanged(it.toInt()) },
            valueRange = 100f..1000f,
        )
    }

}