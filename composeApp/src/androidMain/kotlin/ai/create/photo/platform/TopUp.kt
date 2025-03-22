package ai.create.photo.platform

import ai.create.photo.app.App
import ai.create.photo.core.billing.BillingRepository
import ai.create.photo.ui.settings.balance.Pricing

actual fun topUpPlatform(userId: String, pricing: Pricing) {
    val success = BillingRepository.launchBillingFlow(App.currentActivity!!, pricing)
    if (!success) {
        openUrl("${pricing.paymentLink}?client_reference_id=$userId")
    }
}