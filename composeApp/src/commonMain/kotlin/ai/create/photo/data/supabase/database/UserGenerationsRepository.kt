package ai.create.photo.data.supabase.database

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.model.UserGeneration
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.DownloadStatus
import io.github.jan.supabase.storage.downloadAuthenticatedAsFlow
import io.github.jan.supabase.storage.storage
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi

object UserGenerationsRepository {

    private const val USER_GENERATIONS_TABLE = "user_generations"

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getCreations(
        userId: String,
        page: Int,
        pageSize: Int
    ): Result<List<UserGeneration>> = runCatching {
        Logger.i("getCreations, page: $page, pageSize: $pageSize")
        val from = ((page - 1) * pageSize).toLong()
        val to = (from + pageSize - 1).toLong()
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .select(columns = Columns.list(UserGeneration.columns)) {
                filter {
                    eq("user_id", userId)
                    eq("status", "succeeded")
                }
                order(column = "created_at", order = Order.DESCENDING)
                range(from, to)
            }
            .decodeList<UserGeneration>()
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getCreationsAfter(
        userId: String,
        latestCreatedAt: Instant,
    ): Result<List<UserGeneration>> = runCatching {
        Logger.i("getCreationsAfter $latestCreatedAt")
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .select(columns = Columns.list(UserGeneration.columns)) {
                filter {
                    eq("user_id", userId)
                    eq("status", "succeeded")
                    gt("created_at", latestCreatedAt)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<UserGeneration>().also {
                Logger.i("getCreationsAfter, count: ${it.size}")
            }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getPublicGallery(
        page: Int,
        pageSize: Int
    ): Result<List<UserGeneration>> = runCatching {
        Logger.i("getPublicGallery, page: $page, pageSize: $pageSize")
        val from = ((page - 1) * pageSize).toLong()
        val to = (from + pageSize - 1).toLong()
        Supabase.supabase
            .from(USER_GENERATIONS_TABLE)
            .select(columns = Columns.list(UserGeneration.columns)) {
                filter {
                    eq("status", "succeeded")
                    eq("is_public", true)
                }
                order(column = "created_at", order = Order.DESCENDING)
                range(from, to)
            }
            .decodeList<UserGeneration>()
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getPublicGalleryAfter(latestCreatedAt: Instant): Result<List<UserGeneration>> =
        runCatching {
            Logger.i("getPublicGalleryAfter $latestCreatedAt")
            Supabase.supabase
                .from(USER_GENERATIONS_TABLE)
                .select(columns = Columns.list(UserGeneration.columns)) {
                    filter {
                        eq("status", "succeeded")
                        eq("is_public", true)
                        gt("created_at", latestCreatedAt)
                    }
                    limit(100)
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<UserGeneration>().also {
                    Logger.i("getCreationsAfter, count: ${it.size}")
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

    suspend fun downloadGeneratedPhoto(photoUrl: String) {
        Logger.i("Download photo url - $photoUrl")
        val client = HttpClient()
        try {
            val getPhoto = client.get(photoUrl)
            val photoBytes = getPhoto.readRawBytes()

            val saveFile = FileKit.saveFile(
                bytes = photoBytes,
                baseName = "image",
                extension = "jpg"
            )
            if (saveFile != null) {
                Logger.i("Downloading successfull")
            }

        } catch (e: Exception) {
            Logger.i("Downloading failed with exeption - $e")
        }
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