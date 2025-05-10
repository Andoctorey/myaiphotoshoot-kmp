import StoreKit
import ComposeApp

class InAppPurchaseManager: NSObject, SKProductsRequestDelegate, SKPaymentTransactionObserver {
    static let shared = InAppPurchaseManager()
    
    private var products: [String: SKProduct] = [:]
    private var purchaseCompletion: ((Bool, String?, Error?) -> Void)?
    private var completionHandlers: [String: (SKProduct?) -> Void] = [:]
    
    private override init() {
        super.init()
        SKPaymentQueue.default().add(self)
        LoggerKt.log(message: "InAppPurchaseManager initialized")
    }
    
    static func setup() {
        _ = shared
    }
    
    func requestProduct(productId: String, completion: @escaping (SKProduct?) -> Void) {
        LoggerKt.log(message: "Requesting product: \(productId)")
        if let product = products[productId] {
            LoggerKt.log(message: "Product \(productId) found in cache")
            completion(product)
            return
        }
        let request = SKProductsRequest(productIdentifiers: Set([productId]))
        request.delegate = self
        completionHandlers[productId] = completion
        request.start()
    }
    
    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        LoggerKt.log(message: "Received product response. Products: \(response.products.count), Invalid IDs: \(response.invalidProductIdentifiers)")
        
        for product in response.products {
            LoggerKt.log(message: "Found product: \(product.productIdentifier)")
            products[product.productIdentifier] = product
            if let handler = completionHandlers[product.productIdentifier] {
                handler(product)
                completionHandlers.removeValue(forKey: product.productIdentifier)
            }
        }
        
        for invalidId in response.invalidProductIdentifiers {
            LoggerKt.log(message: "Invalid product ID: \(invalidId)")
            if let handler = completionHandlers[invalidId] {
                handler(nil)
                completionHandlers.removeValue(forKey: invalidId)
            }
        }
    }
    
    func purchaseProduct(_ product: SKProduct, completion: @escaping (Bool, String?, Error?) -> Void) {
        LoggerKt.log(message: "Purchasing product: \(product.productIdentifier)")
        purchaseCompletion = completion
        let payment = SKPayment(product: product)
        SKPaymentQueue.default().add(payment)
    }
    
    func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
        for transaction in transactions {
            switch transaction.transactionState {
            case .purchased:
                LoggerKt.log(message: "Transaction purchased: \(transaction.payment.productIdentifier)")
                completeTransaction(transaction)
            case .failed:
                LoggerKt.log(message: "Transaction failed: \(transaction.payment.productIdentifier), Error: \(transaction.error?.localizedDescription ?? "Unknown")")
                failTransaction(transaction)
            case .restored:
                LoggerKt.log(message: "Transaction restored: \(transaction.payment.productIdentifier)")
                SKPaymentQueue.default().finishTransaction(transaction)
            case .deferred:
                LoggerKt.log(message: "Transaction deferred: \(transaction.payment.productIdentifier)")
            case .purchasing:
                LoggerKt.log(message: "Transaction purchasing: \(transaction.payment.productIdentifier)")
            @unknown default:
                LoggerKt.log(message: "Unknown transaction state: \(transaction.payment.productIdentifier)")
            }
        }
    }
    
    private func completeTransaction(_ transaction: SKPaymentTransaction) {
        if let receiptURL = Bundle.main.appStoreReceiptURL,
           let receiptData = try? Data(contentsOf: receiptURL) {
            let receiptString = receiptData.base64EncodedString()
            LoggerKt.log(message: "Purchase completed with receipt")
            purchaseCompletion?(true, receiptString, nil)
        } else {
            LoggerKt.log(message: "Failed to retrieve receipt")
            purchaseCompletion?(false, nil, nil)
        }
        purchaseCompletion = nil
        SKPaymentQueue.default().finishTransaction(transaction)
    }
    
    private func failTransaction(_ transaction: SKPaymentTransaction) {
        LoggerKt.error(message: "Purchase failed with error: \(transaction.error?.localizedDescription ?? "Unknown")")
        purchaseCompletion?(false, nil, transaction.error)
        purchaseCompletion = nil
        SKPaymentQueue.default().finishTransaction(transaction)
    }
}
