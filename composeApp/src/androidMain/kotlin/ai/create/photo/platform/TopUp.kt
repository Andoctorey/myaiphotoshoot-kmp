package ai.create.photo.platform

import ai.create.photo.app.App
import ai.create.photo.core.billing.BillingRepository
import ai.create.photo.core.extention.toast
import ai.create.photo.ui.settings.balance.Pricing

actual suspend fun topUpPlatform(userId: String, pricing: Pricing) {
    val activity = App.currentActivity ?: return
    BillingRepository.purchase(activity, pricing).onFailure {
        activity.toast(it.message)
//        openUrl("${pricing.paymentLink}?client_reference_id=$userId")
    }
}