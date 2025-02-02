package ai.create.photo.data.supabase.database

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.model.UserTraining
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

object UserTrainingsRepository {

    private const val USER_TRAININGS_TABLE = "user_trainings"

    suspend fun getTrainings(userId: String): Result<List<UserTraining>> = runCatching {
        Supabase.supabase
            .from(USER_TRAININGS_TABLE)
            .select(columns = Columns.list(UserTraining.columns)) {
                filter {
                    eq("user_id", userId)
                    eq("status", "succeeded")
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<UserTraining>()
            .also {
                Logger.i("getTrainings: ${it.size}")
            }
    }

    suspend fun getLatestTraining(userId: String): Result<UserTraining?> = runCatching {
        Supabase.supabase
            .from(USER_TRAININGS_TABLE)
            .select(columns = Columns.list(UserTraining.columns)) {
                filter {
                    eq("user_id", userId)
                }
                limit(1)
            }
            .decodeSingleOrNull<UserTraining>()
            .also {
                Logger.i("getTraining: $it")
            }
    }

    suspend fun updatePersonDescription(id: String, personDescription: String): Result<Unit> =
        runCatching {
            Supabase.supabase
                .from(USER_TRAININGS_TABLE)
                .update(mapOf("person_description" to personDescription)) {
                    filter {
                        eq("id", id)
                    }
                }
        }
}