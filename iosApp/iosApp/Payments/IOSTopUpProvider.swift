import ComposeApp
import StoreKit

class IOSTopUpProvider: TopUpProvider {
    func topUp(userId: String,
               pricing: Pricing,
               onSuccess: @escaping () -> Void,
               onFailure: @escaping (KotlinThrowable?) -> Void
    ) {
        LoggerKt.log(message: "Starting top-up for user: \(userId), product: \(pricing.productId)")
        
        InAppPurchaseManager.shared.requestProduct(productId: pricing.productId) { product in
            LoggerKt.log(message: "Product request completed. Product found: \(product != nil)")
            
            guard let product = product else {
                LoggerKt.log(message: "Failed to fetch product with ID: \(pricing.productId)")
                onFailure(KotlinRuntimeException(message: "Failed to fetch product with ID: \(pricing.productId)"))
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
                            onSuccess()
                        },
                        onFailure: { throwable in
                            onFailure(KotlinThrowable(message: throwable?.message))
                            LoggerKt.error(message: "Server validation failed: \(throwable?.description() ?? "")")
                        }
                    )
                } else {
                    let errorMessage = error?.localizedDescription ?? "Purchase canceled"
                    LoggerKt.log(message: "Purchase failed or canceled. Error: \(errorMessage)")
                    
                    // Check if user canceled the purchase
                    if let error = error as? SKError, error.code == .paymentCancelled {
                        LoggerKt.log(message: "User canceled the purchase")
                        onFailure(nil)
                    } else {
                        // Other purchase failures
                        let failureMessage = error?.localizedDescription ?? "Purchase failed"
                        onFailure(KotlinRuntimeException(message: failureMessage))
                    }
                }
            }
        }
    }
}
