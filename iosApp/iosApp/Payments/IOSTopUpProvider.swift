import ComposeApp
import StoreKit

class IOSTopUpProvider: TopUpProvider {
    func topUp(userId: String, pricing: Pricing, onBalanceUpdated: @escaping () -> Void) {
        print("Starting top-up for user: \(userId), product: \(pricing.productId)")
        
        InAppPurchaseManager.shared.requestProduct(productId: pricing.productId) { product in
            print("Product request completed. Product found: \(product != nil)")
            
            guard let product = product else {
                print("Failed to fetch product with ID: \(pricing.productId)")
                onBalanceUpdated()
                return
            }
            
            print("Initiating purchase for product: \(product.productIdentifier)")
            InAppPurchaseManager.shared.purchaseProduct(product) { success, receipt, error in
                if success, let receipt = receipt {
                    print("Purchase succeeded. Receipt: \(receipt.prefix(50))...")
                    TopUpKt.handlePurchaseCompletion(
                        userId: userId,
                        pricing: pricing,
                        receipt: receipt,
                        onSuccess: {
                            print("Server validation succeeded")
                            onBalanceUpdated()
                        },
                        onFailure: { error in
                            print("Server validation failed: \(error.description())")
                            onBalanceUpdated()
                        }
                    )
                } else {
                    print("Purchase failed or canceled. Error: \(error?.localizedDescription ?? "None")")
                    onBalanceUpdated()
                }
            }
        }
    }
}
