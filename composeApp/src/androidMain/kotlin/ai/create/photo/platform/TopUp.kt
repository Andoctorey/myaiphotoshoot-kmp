package ai.create.photo.platform

import ai.create.photo.app.App
import ai.create.photo.core.billing.BillingRepository
import ai.create.photo.ui.settings.balance.Pricing

actual suspend fun topUpPlatform(
    userId: String,
    pricing: Pricing,
    onSuccess: () -> Unit,
    onFailure: (e: Throwable?) -> Unit,
) {
    val activity = App.currentActivity ?: return
    BillingRepository.purchase(
        activity = activity,
        pricing = pricing,
        onSuccess = onSuccess,
        onFailure = {
            if (it != null) {
                openUrl("${pricing.paymentLink}?client_reference_id=$userId")
            }
            onFailure(it)
        }
    )
}