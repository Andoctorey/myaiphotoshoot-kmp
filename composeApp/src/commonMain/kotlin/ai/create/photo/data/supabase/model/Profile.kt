package ai.create.photo.data.supabase.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlin.math.abs
import kotlin.math.round

@Serializable
data class Profile(
    @SerialName("balance") val balance: Float,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val columns = serializer().descriptor.elementNames.toList()
    }

    val formattedBalance: String?
        get() {
            val rounded = round(balance * 100) / 100
            val result = if (abs(balance - rounded) < 1e-10) {
                balance
            } else {
                rounded
            }.toString()
            return if ('.' in result) {
                result.trimEnd('0').trimEnd('.')
            } else {
                result
            }
        }
}