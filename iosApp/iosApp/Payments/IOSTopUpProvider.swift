import ComposeApp
import StoreKit

class IOSTopUpProvider: TopUpProvider {
    func topUp(userId: String, pricing: Pricing, onBalanceUpdated: @escaping () -> Void) {
        LoggerKt.log(message: "Starting top-up for user: \(userId), product: \(pricing.productId)")
        
        InAppPurchaseManager.shared.requestProduct(productId: pricing.productId) { product in
            LoggerKt.log(message: "Product request completed. Product found: \(product != nil)")
            
            guard let product = product else {
                LoggerKt.log(message: "Failed to fetch product with ID: \(pricing.productId)")
                return
            }
            
            LoggerKt.log(message: "Initiating purchase for product: \(product.productIdentifier)")
            InAppPurchaseManager.shared.purchaseProduct(product) { success, receipt, transactionId, error in
                if success, let receipt = receipt, let transactionId = transactionId {
                    LoggerKt.log(message: "Purchase succeeded. Receipt: \(receipt.prefix(50))..., TransactionId: \(transactionId)")
                    TopUpKt.handlePurchaseCompletion(
                        pricing: pricing,
                        receipt: receipt,
                        transactionId: transactionId,
                        onSuccess: {
                            LoggerKt.log(message: "Server validation succeeded")
                            onBalanceUpdated()
                        },
                        onFailure: { error in
                            LoggerKt.error(message: "Server validation failed: \(error.description())")
                        }
                    )
                } else {
                    let errorMessage = error?.localizedDescription ?? "Purchase canceled"
                    LoggerKt.log(message: "Purchase failed or canceled. Error: \(errorMessage)")
                }
            }
        }
    }
}
