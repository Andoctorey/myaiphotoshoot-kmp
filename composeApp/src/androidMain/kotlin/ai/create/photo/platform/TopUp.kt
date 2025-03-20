package ai.create.photo.platform

import ai.create.photo.app.App
import ai.create.photo.core.billing.BillingRepository
import ai.create.photo.ui.settings.balance.Pricing

actual fun topUpPlatform(userId: String, pricing: Pricing) {
    // TODO init BillingRepository(App.context) on app start
    val success = BillingRepository(App.context).launchBillingFlow(App.currentActivity!!, pricing)
    if (!success) {
        openUrl("${pricing.paymentLink}?client_reference_id=$userId")
    }
}