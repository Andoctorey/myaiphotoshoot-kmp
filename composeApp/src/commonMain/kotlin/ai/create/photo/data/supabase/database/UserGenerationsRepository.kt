package ai.create.photo.data.supabase.database

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.model.UserGeneration
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.elementNames

object UserGenerationsRepository {

    private const val USER_GENERATIONS_TABLE = "user_generations"

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getGenerations(
        userId: String,
        page: Int,
        pageSize: Int
    ): Result<List<UserGeneration>> = runCatching {
        Logger.i("getGenerations, page: $page, pageSize: $pageSize")
        val from = ((page - 1) * pageSize).toLong()
        val to = (from + pageSize - 1).toLong()
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .select(columns = Columns.list(UserGeneration.serializer().descriptor.elementNames.toList())) {
                filter {
                    eq("user_id", userId)
                    eq("status", "succeeded")
                }
                order(column = "created_at", order = Order.DESCENDING)
                range(from, to)
            }
            .decodeList<UserGeneration>()
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

    suspend fun setPublic(photoId: String, public: Boolean) {
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .update(mapOf("is_public" to public)) {
                filter {
                    eq("id", photoId)
                }
            }
            .also { Logger.i("makePublic: $photoId") }
    }
}