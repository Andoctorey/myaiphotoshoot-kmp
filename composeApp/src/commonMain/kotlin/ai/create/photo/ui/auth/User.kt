package ai.create.photo.ui.auth

import kotlin.math.abs
import kotlin.math.round

data class User(
    val id: String,
    val email: String?,
    val balance: Float?,
) {
    val formattedBalance: String?
        get() = balance?.let {
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
