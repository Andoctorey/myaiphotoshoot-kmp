package ai.create.photo.platform

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.ui.settings.balance.Pricing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

interface TopUpProvider {
    fun topUp(
        userId: String,
        pricing: Pricing,
        onSuccess: () -> Unit,
        onFailure: (e: Throwable?) -> Unit
    )
}

var topUpProvider: TopUpProvider? = null

actual suspend fun topUpPlatform(
    userId: String,
    pricing: Pricing,
    onSuccess: () -> Unit,
    onFailure: (e: Throwable?) -> Unit
) {
    topUpProvider?.topUp(userId, pricing, onSuccess, onFailure)
        ?: onFailure(IllegalStateException("TopUpProvider not set"))
}

@Suppress("unused")
fun handlePurchaseCompletion(
    pricing: Pricing,
    receipt: String,
    transactionId: String,
    onSuccess: () -> Unit,
    onFailure: (Throwable?) -> Unit
) = CoroutineScope(Dispatchers.IO).launch {
    try {
        val result = SupabaseFunction.verifyIosPurchase(
            productId = pricing.productId,
            receipt = receipt,
            transactionId = transactionId,
        )
        if (result) {
            onSuccess()
        } else {
            onFailure(Exception("Purchase verification failed"))
        }
    } catch (e: Exception) {
        onFailure(e)
    }
}





