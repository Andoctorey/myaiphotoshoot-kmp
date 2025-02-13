package ai.create.photo.ui.generate

import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedButton
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
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.ai_model
import photocreateai.composeapp.generated.resources.enhance_photo_accuracy
import photocreateai.composeapp.generated.resources.enhance_prompt
import photocreateai.composeapp.generated.resources.photo_prompt
import photocreateai.composeapp.generated.resources.photos_to_generate
import photocreateai.composeapp.generated.resources.picture_to_prompt
import photocreateai.composeapp.generated.resources.surprise_me
import photocreateai.composeapp.generated.resources.train_ai_model
import photocreateai.composeapp.generated.resources.translate


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel = viewModel { GenerateViewModel() },
    trainAiModel: () -> Unit,
    onGenerate: (String, String, Int) -> Unit,
    prompt: String,
) {
    LaunchedEffect(Unit) {
        viewModel.loadTrainings()
    }

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

            val hasSoftKeyboard = remember {
                platform().platform in
                        listOf(Platforms.ANDROID, Platforms.IOS, Platforms.WEB_MOBILE)
            }

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxSize()
                    .animateContentSize().verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (hasSoftKeyboard) Arrangement.SpaceBetween else Arrangement.SpaceAround
            ) {

                // top of the screen
                Card(
                    modifier = Modifier.systemBarsPadding().padding(8.dp),
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
                        if (!state.trainings.isNullOrEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Trainings(
                                    modifier = Modifier.align(Alignment.Center)
                                        .padding(top = 12.dp),
                                    trainings = state.trainings,
                                    selectedTraining = state.training,
                                    selectTraining = viewModel::selectTraining,
                                    trainAiModel = trainAiModel,
                                )
                                IconButton(
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                        .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                                    onClick = viewModel::toggleSettings,
                                ) {
                                    Icon(
                                        imageVector = if (state.showSettings) Icons.Default.Close else Icons.Default.Settings,
                                        contentDescription = "settings",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            Crossfade(targetState = state.showSettings) { showSettings ->
                                if (showSettings) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (state.personDescription.isNotEmpty()) {
                                            EnhancePhotoAccuracy(
                                                personDescription = state.personDescription,
                                                onPersonDescriptionChanged = viewModel::onPersonDescriptionChanged,
                                                isLoadingPersonDescription = state.isLoadingPersonDescription,
                                                onRefreshPersonDescription = viewModel::onRefreshPersonDescription,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        PhotosToGenerate(state.photosToGenerateX100) {
                                            viewModel.onPhotosToGenerateChanged(it)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // middle of the screen
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                        .animateContentSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.trainings.isNullOrEmpty()) {
                        CreateAiModelButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            onClick = trainAiModel,
                        )
                    }

                    if (!state.trainings.isNullOrEmpty()) {
                        SurpriseMeButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            isLoading = state.isLoadingSurpriseMe,
                            onClick = viewModel::surpriseMe
                        )

                        EnhancePromptButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            isLoading = state.isEnhancingPrompt,
                            onClick = viewModel::enhancePrompt
                        )

                        val launcher = rememberFilePickerLauncher(
                            title = stringResource(Res.string.picture_to_prompt),
                            type = PickerType.Image,
                            mode = PickerMode.Single
                        ) { file ->
                            if (file == null) return@rememberFilePickerLauncher
                            viewModel.pictureToPrompt(file)
                        }
                        PictureToPromptButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            isLoading = state.isLoadingPictureToPrompt,
                            onClick = { launcher.launch() }
                        )

                        TranslateButton(
                            modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth(),
                            show = state.showTranslateButton,
                            isTranslating = state.isTranslating,
                            onClick = viewModel::translate,
                        )
                    }
                }

                // bottom of the screen
                var previousText by remember { mutableStateOf("") }
                LaunchedEffect(state.userPrompt) {
                    if (state.userPrompt.count { it == '\n' } > previousText.count { it == '\n' }) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                    previousText = state.userPrompt
                }

                PhotoPrompt(prompt = state.userPrompt,
                    onPromptChanged = viewModel::onUserPromptChanged,
                    onGenerate = { viewModel.prepareToGenerate(onGenerate, trainAiModel) })
            }
        }

        if (state.errorPopup != null) {
            ErrorPopup(state.errorPopup) {
                viewModel.hideErrorPopup()
            }
        }

        LaunchedEffect(prompt) {
            if (prompt.isNotEmpty()) {
                viewModel.putPrompt(prompt)
            }
        }
    }
}


@Composable
private fun EnhancePhotoAccuracy(
    personDescription: String, onPersonDescriptionChanged: (String) -> Unit,
    isLoadingPersonDescription: Boolean = false, onRefreshPersonDescription: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 24.dp)
            .animateContentSize(),
        value = personDescription,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoadingPersonDescription) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        modifier = Modifier.clickable { onRefreshPersonDescription() },
                        imageVector = Icons.Default.Refresh,
                        contentDescription = Icons.Default.Refresh.name,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        onValueChange = onPersonDescriptionChanged,
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
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
        ),
        maxLines = 10,
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
private fun CreateAiModelButton(modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = stringResource(Res.string.train_ai_model)
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = stringResource(Res.string.train_ai_model),
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun EnhancePromptButton(modifier: Modifier, isLoading: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        OutlinedButton(
            modifier = modifier.alpha(if (isLoading) 0f else 1f),
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = stringResource(Res.string.enhance_prompt)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
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
private fun PictureToPromptButton(modifier: Modifier, isLoading: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        OutlinedButton(
            modifier = modifier.alpha(if (isLoading) 0f else 1f),
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = stringResource(Res.string.picture_to_prompt)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = stringResource(Res.string.picture_to_prompt),
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
private fun TranslateButton(
    modifier: Modifier,
    show: Boolean,
    isTranslating: Boolean,
    onClick: () -> Unit
) {
    Crossfade(targetState = show) {
        if (it) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                OutlinedButton(
                    modifier = Modifier.alpha(if (isTranslating) 0f else 1f),
                    onClick = onClick
                ) {
                    val icon = Icons.Default.Translate
                    Icon(
                        imageVector = icon,
                        contentDescription = icon.name,
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(Res.string.translate),
                        fontSize = 16.sp,
                    )
                }
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(horizontal = 8.dp).size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SurpriseMeButton(modifier: Modifier, isLoading: Boolean, onClick: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        OutlinedButton(
            modifier = Modifier.alpha(if (isLoading) 0f else 1f).padding(horizontal = 4.dp),
            onClick = onClick,
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = stringResource(Res.string.surprise_me)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
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
    trainAiModel: () -> Unit
) {
    val options = trainings.toMutableList()
    options.add(null)
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(selectedTraining) }
    val aiModelString = stringResource(Res.string.ai_model)
    val createAiModelString = stringResource(Res.string.train_ai_model)
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
                        if (selectedOption == null) trainAiModel()
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