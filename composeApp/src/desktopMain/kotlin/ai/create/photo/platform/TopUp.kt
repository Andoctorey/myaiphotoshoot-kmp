package ai.create.photo.platform

import ai.create.photo.ui.settings.balance.Pricing

actual suspend fun topUpPlatform(userId: String, pricing: Pricing) {
    openUrl("${pricing.paymentLink}?client_reference_id=$userId")
}

