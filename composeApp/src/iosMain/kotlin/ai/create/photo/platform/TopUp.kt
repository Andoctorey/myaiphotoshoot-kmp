package ai.create.photo.platform

import ai.create.photo.ui.settings.balance.Pricing

interface TopUpProvider {
    fun topUp(userId: String, pricing: Pricing, onBalanceUpdated: () -> Unit)
}

var topUpProvider: TopUpProvider? = null

actual suspend fun topUpPlatform(userId: String, pricing: Pricing, onBalanceUpdated: () -> Unit) =
    topUpProvider?.topUp(userId, pricing, onBalanceUpdated)
        ?: throw IllegalStateException("TopUpProvider not set")





