package ai.create.photo.data.model

import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Base64String(val value: String)

@OptIn(ExperimentalEncodingApi::class)
inline fun <reified T> T.toBase64String(): Base64String where T : @Serializable Any {
    val promptJson = Json.encodeToString(this)
    val promptBase64 = Base64.UrlSafe.encode(promptJson.encodeToByteArray())
    return Base64String(promptBase64)
}

@OptIn(ExperimentalEncodingApi::class)
inline fun <reified T : @Serializable Any> Base64String.parse(): T? {
    return try {
        val promptBytes = Base64.UrlSafe.decode(value)
        val promptJson = promptBytes.decodeToString()
        Json.decodeFromString<T>(promptJson)
    } catch (e: Exception) {
        Logger.e("Failed to decode prompt", e)
        null
    }
}