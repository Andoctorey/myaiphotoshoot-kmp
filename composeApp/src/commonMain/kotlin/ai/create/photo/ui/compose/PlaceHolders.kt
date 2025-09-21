package ai.create.photo.ui.compose

import ai.create.photo.data.supabase.model.ErrorResponse
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.connection_error

@Preview
@Composable
fun LoadingPlaceholder(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(ButtonDefaults.MinHeight),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 4.dp,
    )
}

@Composable
// ai.create.photo.data.logger.SlackLogWriter.log
fun Throwable.getFriendlyError() = when (this) {
    is HttpRequestException -> stringResource(Res.string.connection_error)
    is IOException -> stringResource(Res.string.connection_error)
    is UnresolvedAddressException -> stringResource(Res.string.connection_error)
    is RestException -> {
        var message = this.error
        try {
            message = Json.decodeFromString<ErrorResponse>(message).error
        } catch (_: Exception) {
        }
        message
    }

    else -> this.message.toString()
}

@Preview
@Composable
fun ErrorMessagePlaceHolder(errorMessage: Throwable) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = errorMessage.message,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        BasicText(
            text = errorMessage.getFriendlyError(),
            autoSize = TextAutoSize.StepBased(
                minFontSize = 10.sp,
                maxFontSize = 24.sp,
                stepSize = 1.sp
            ),
            style = TextStyle(
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun ErrorMessagePlaceHolderSmall(errorMessage: Throwable) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = errorMessage.message,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicText(
            text = errorMessage.getFriendlyError(),
            autoSize = TextAutoSize.StepBased(
                minFontSize = 6.sp,
                maxFontSize = 14.sp,
                stepSize = 1.sp
            ),
            style = TextStyle(
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        )
    }
}