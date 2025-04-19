package ai.create.photo.platform

import ai.create.photo.ui.settings.balance.Pricing

expect suspend fun topUpPlatform(userId: String, pricing: Pricing, onBalanceUpdated: () -> Unit)