package ai.create.photo.ui.article

import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.ui.compose.ErrorMessagePlaceHolder
import ai.create.photo.ui.compose.ErrorMessagePlaceHolderSmall
import ai.create.photo.ui.compose.LoadingPlaceholder
import ai.create.photo.ui.generate.Prompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
private fun ArticleScreenPreview() = ArticleScreen(
    postId = "preview",
    onBackClick = {},
    openGenerateTab = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    postId: String,
    onBackClick: () -> Unit,
    openGenerateTab: (Prompt) -> Unit,
    viewModel: ArticleViewModel = viewModel { ArticleViewModel() }
) {
    val state = viewModel.uiState

    LaunchedEffect(postId) {
        viewModel.loadArticle(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.title,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    LoadingPlaceholder()
                }

                state.loadingError != null -> {
                    ErrorMessagePlaceHolder(state.loadingError)
                }

                state.topics.isNotEmpty() -> {
                    ArticleContent(
                        topics = state.topics,
                        generate = openGenerateTab
                    )
                }
            }
        }
    }

}

@Composable
private fun ArticleContent(
    topics: List<PhotoTopic>,
    generate: (Prompt) -> Unit
) {
    val listState = rememberLazyGridState()
    val density = LocalDensity.current
    val width = 420
    val minSize = remember { with(density) { (width - 20).toDp() } } // Same as PublicScreen

    LazyVerticalGrid(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(minSize = minSize),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        }

        topics.forEach { topic ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopicHeader(topic = topic)
            }

            topic.photos.forEach { photo ->
                item {
                    PhotoItem(
                        photo = photo,
                        generate = generate
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(24.dp))
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}

@Composable
private fun TopicHeader(topic: PhotoTopic) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = topic.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (topic.description.isNotEmpty()) {
            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun PhotoItem(
    photo: UserGeneration,
    generate: (Prompt) -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }
    error?.let {
        ErrorMessagePlaceHolderSmall(it)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                generate(Prompt(photo.id, photo.prompt, photo.imageUrl))
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data("${photo.imageUrl}?width=420")
                .crossfade(true)
                .build(),
            contentDescription = photo.prompt,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            onError = {
                Logger.e("error loading image ${photo.imageUrl}", it.result.throwable)
                error = it.result.throwable
            }
        )
    }
}
