package ai.create.photo.data.supabase.database

import ai.create.photo.data.supabase.Supabase
import ai.create.photo.data.supabase.model.Profile
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ProfilesRepository {

    private const val PROFILES_TABLE = "profiles"

    private val _profileFlow = MutableStateFlow<Profile?>(null)
    val profileFlow: StateFlow<Profile?> = _profileFlow
    val profile: Profile?
        get() = profileFlow.value

    suspend fun loadProfile(userId: String) = Supabase.supabase
        .from(PROFILES_TABLE)
        .select(columns = Columns.list(Profile.columns)) {
            filter {
                eq("id", userId)
            }
            limit(1)
        }
        .decodeSingleOrNull<Profile>()
        .also {
            _profileFlow.value = it
            Logger.i("getProfile: $it")
        }
}