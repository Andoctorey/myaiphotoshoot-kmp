package ai.create.photo.data.supabase.database

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.model.UserGeneration
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

object UserGenerationsRepository {

    private const val USER_GENERATIONS_TABLE = "user_generations"

    suspend fun getGenerations(userId: String): Result<List<UserGeneration>> = runCatching {
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .select(Columns.raw("*, user_files(*)")) {
                filter {
                    eq("user_id", userId)
                    eq("status", "succeeded")
                }
                limit(10)
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<UserGeneration>()
            .also {
                Logger.i("getGenerations: ${it.size}, ")
            }
    }

    suspend fun deleteGeneratedPhoto(photoId: String) {
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .delete {
                filter {
                    eq("id", photoId)
                }
            }
            .also { Logger.i("deleteGeneratedPhotoId: $photoId") }
    }
}