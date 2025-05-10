import StoreKit

class InAppPurchaseManager: NSObject, SKProductsRequestDelegate, SKPaymentTransactionObserver {
    static let shared = InAppPurchaseManager()
    
    private var productRequest: SKProductsRequest?
    private var products: [String: SKProduct] = [:]
    private var purchaseCompletion: ((Bool, String?, Error?) -> Void)?
    private var completionHandlers: [String: (SKProduct?) -> Void] = [:]
    
    private override init() {
        super.init()
        SKPaymentQueue.default().add(self)
        print("InAppPurchaseManager initialized")
    }
    
    func requestProduct(productId: String, completion: @escaping (SKProduct?) -> Void) {
        print("Requesting product: \(productId)")
        if let product = products[productId] {
            print("Product \(productId) found in cache")
            completion(product)
            return
        }
        let request = SKProductsRequest(productIdentifiers: Set([productId]))
        request.delegate = self
        productRequest = request
        completionHandlers[productId] = completion
        request.start()
    }
    
    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        print("Received product response. Products: \(response.products.count), Invalid IDs: \(response.invalidProductIdentifiers)")
        for product in response.products {
            print("Found product: \(product.productIdentifier)")
            products[product.productIdentifier] = product
        }
        if let product = response.products.first, let handler = completionHandlers[product.productIdentifier] {
            handler(product)
            completionHandlers.removeValue(forKey: product.productIdentifier)
        } else if let productId = completionHandlers.keys.first {
            print("No product found for \(productId)")
            completionHandlers[productId]?(nil)
            completionHandlers.removeValue(forKey: productId)
        }
        productRequest = nil
    }
    
    func purchaseProduct(_ product: SKProduct, completion: @escaping (Bool, String?, Error?) -> Void) {
        print("Purchasing product: \(product.productIdentifier)")
        purchaseCompletion = completion
        let payment = SKPayment(product: product)
        SKPaymentQueue.default().add(payment)
    }
    
    func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
        for transaction in transactions {
            switch transaction.transactionState {
            case .purchased:
                print("Transaction purchased: \(transaction.payment.productIdentifier)")
                completeTransaction(transaction)
            case .failed:
                print("Transaction failed: \(transaction.payment.productIdentifier), Error: \(transaction.error?.localizedDescription ?? "Unknown")")
                failTransaction(transaction)
            case .restored:
                print("Transaction restored: \(transaction.payment.productIdentifier)")
                SKPaymentQueue.default().finishTransaction(transaction)
            case .deferred:
                print("Transaction deferred: \(transaction.payment.productIdentifier)")
            case .purchasing:
                print("Transaction purchasing: \(transaction.payment.productIdentifier)")
            @unknown default:
                print("Unknown transaction state: \(transaction.payment.productIdentifier)")
            }
        }
    }
    
    private func completeTransaction(_ transaction: SKPaymentTransaction) {
        if let receiptURL = Bundle.main.appStoreReceiptURL,
           let receiptData = try? Data(contentsOf: receiptURL) {
            let receiptString = receiptData.base64EncodedString()
            print("Purchase completed with receipt")
            purchaseCompletion?(true, receiptString, nil)
        } else {
            print("Failed to retrieve receipt")
            purchaseCompletion?(false, nil, nil)
        }
        purchaseCompletion = nil
        SKPaymentQueue.default().finishTransaction(transaction)
    }
    
    private func failTransaction(_ transaction: SKPaymentTransaction) {
        print("Purchase failed with error: \(transaction.error?.localizedDescription ?? "Unknown")")
        purchaseCompletion?(false, nil, transaction.error)
        purchaseCompletion = nil
        SKPaymentQueue.default().finishTransaction(transaction)
    }
}
