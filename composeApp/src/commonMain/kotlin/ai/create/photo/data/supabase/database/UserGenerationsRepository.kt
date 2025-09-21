package ai.create.photo.data.supabase.database

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.model.GenerationsFilter
import ai.create.photo.data.supabase.model.GenerationsSort
import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.data.supabase.retryWithBackoff
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object UserGenerationsRepository {

    private const val USER_GENERATIONS_TABLE = "user_generations"

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getCreations(
        userId: String,
        page: Int,
        pageSize: Int,
        filter: GenerationsFilter,
    ): Result<List<UserGeneration>> = runCatching {
        retryWithBackoff {
            Logger.i("getCreations, page: $page, pageSize: $pageSize")
            val from = ((page - 1) * pageSize).toLong()
            val to = (from + pageSize - 1)
            Supabase.supabase
                .from(USER_GENERATIONS_TABLE)
                .select(columns = Columns.list(UserGeneration.columns)) {
                    filter {
                        eq("user_id", userId)
                        eq("status", "succeeded")
                        when (filter) {
                            GenerationsFilter.ALL -> {}
                            GenerationsFilter.PUBLIC -> eq("is_public", true)
                        }
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<UserGeneration>()
        }
    }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
    suspend fun getCreationsAfter(
        userId: String,
        latestCreatedAt: Instant?,
        filter: GenerationsFilter,
    ): Result<List<UserGeneration>> = runCatching {
        retryWithBackoff {
            Logger.i("getCreationsAfter $latestCreatedAt")
            Supabase.supabase
                .from(USER_GENERATIONS_TABLE)
                .select(columns = Columns.list(UserGeneration.columns)) {
                    filter {
                        eq("user_id", userId)
                        eq("status", "succeeded")
                        when (filter) {
                            GenerationsFilter.ALL -> {}
                            GenerationsFilter.PUBLIC -> eq("is_public", true)
                        }
                        if (latestCreatedAt != null) {
                            gt("created_at", latestCreatedAt)
                        }
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<UserGeneration>().also {
                    Logger.i("getCreationsAfter, count: ${it.size}")
                }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getPublicGallery(
        page: Int,
        pageSize: Int,
        sortOrder: GenerationsSort,
    ): Result<List<UserGeneration>> = runCatching {
        retryWithBackoff {
            Logger.i("getPublicGallery, sort: $sortOrder, page: $page, pageSize: $pageSize")
            val from = ((page - 1) * pageSize).toLong()
            val to = (from + pageSize - 1)
            Supabase.supabase
                .from(USER_GENERATIONS_TABLE)
                .select(columns = Columns.list(UserGeneration.columns)) {
                    filter {
                        eq("status", "succeeded")
                        eq("is_public", true)
                    }
                    when (sortOrder) {
                        GenerationsSort.NEW -> order(
                            column = "created_at",
                            order = Order.DESCENDING
                        )

                        GenerationsSort.POPULAR -> order(
                            column = "popularity",
                            order = Order.DESCENDING
                        )
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                    range(from, to)
                }
                .decodeList<UserGeneration>()
        }
    }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
    suspend fun getPublicGalleryAfter(latestCreatedAt: Instant): Result<List<UserGeneration>> =
        runCatching {
            retryWithBackoff {
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
        }

    suspend fun deleteGeneratedPhoto(photoId: String) {
        retryWithBackoff {
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

    suspend fun downloadGeneratedPhoto(id: String, photoUrl: String) {
        val bytes = HttpClient().get(photoUrl).readRawBytes()
        FileKit.saveFile(bytes = bytes, baseName = id, extension = "jpg")
    }

    suspend fun setPublic(photoId: String, public: Boolean) {
        retryWithBackoff {
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
}