package ai.create.photo.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun LoadingPlaceholderPreview() = LoadingPlaceholder()

@Composable
fun LoadingPlaceholder() {
    CircularProgressIndicator(
        modifier = Modifier.size(ButtonDefaults.MinHeight),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 4.dp,
    )
}

@Preview
@Composable
private fun ErrorMessagePlaceHolderPreview() =
    ErrorMessagePlaceHolder("Error message Error message Error message")


@Composable
fun ErrorMessagePlaceHolder(errorMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = errorMessage,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun ErrorMessagePreview() =
    ErrorMessage("Error message Error message Error message Error message")

@Composable
fun ErrorMessage(errorMessage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = errorMessage,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}