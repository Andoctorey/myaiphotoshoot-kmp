package ai.create.photo.supabase.database

import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.model.UserTraining
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from

object UserTrainingsRepository {

    private const val USER_TRAININGS_TABLE = "user_trainings"

    suspend fun getTraining(userId: String, photoSet: Int): Result<UserTraining?> = runCatching {
        supabase
            .from(USER_TRAININGS_TABLE)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("photo_set", photoSet)
                }
            }
            .decodeSingleOrNull<UserTraining>()
            .also {
                Logger.i("getTraining: $it")
            }
    }
}