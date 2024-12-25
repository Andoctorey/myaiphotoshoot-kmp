package ai.create.photo.supabase

import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object Supabase {

    val supabase = createSupabaseClient(
        supabaseUrl = if (platform().platform == Platforms.DESKTOP) "http://127.0.0.1:54321" else "https://trzgfajvyjpvbqedyxug.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRyemdmYWp2eWpwdmJxZWR5eHVnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzM1NTA5MzAsImV4cCI6MjA0OTEyNjkzMH0.39Qdq2nTCuoIpAfc7L725MZA2ls3NegFy6zCjOTzW9M"
    ) {
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
        install(Auth)
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)
        install(Functions)
    }
}
