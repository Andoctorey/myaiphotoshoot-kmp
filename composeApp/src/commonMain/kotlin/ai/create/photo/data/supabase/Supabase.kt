package ai.create.photo.data.supabase

import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

object Supabase {

    const val LOCAL = false

    val supabase = createSupabaseClient(
        supabaseUrl = if (LOCAL) (if (platform().platform in listOf(
                Platforms.DESKTOP, Platforms.WEB_DESKTOP,
                Platforms.WEB_MOBILE
            )
        ) "http://127.0.0.1:54321" else "http://192.168.1.101:54321")
        else "https://trzgfajvyjpvbqedyxug.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRyemdmYWp2eWpwdmJxZWR5eHVnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzM1NTA5MzAsImV4cCI6MjA0OTEyNjkzMH0.39Qdq2nTCuoIpAfc7L725MZA2ls3NegFy6zCjOTzW9M"
    ) {
        requestTimeout = 2.minutes
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
        install(Functions)
    }.also { if (LOCAL) Logger.w("Using local supabase") }
}
