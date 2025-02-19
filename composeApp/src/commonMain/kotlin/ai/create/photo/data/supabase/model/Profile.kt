package ai.create.photo.data.supabase.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToLong

@Serializable
data class Profile(
    @SerialName("balance") val balance: Float,
    @SerialName("preferences") val preferences: Preferences,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val columns = serializer().descriptor.elementNames.toList()
    }

    val formattedBalance: String
        get() {
            val rounded = round(balance * 100) / 100
            val valueToFormat = if (abs(balance - rounded) < 1e-10) balance else rounded
            val scaled = (valueToFormat * 100).roundToLong()
            val intPart = scaled / 100
            val fracPart = (scaled % 100).toInt()

            return if (fracPart == 0) {
                intPart.toString()
            } else {
                val frac = if (fracPart < 10) "0$fracPart" else fracPart.toString()
                "$intPart.${frac.trimEnd('0')}"
            }
        }


}