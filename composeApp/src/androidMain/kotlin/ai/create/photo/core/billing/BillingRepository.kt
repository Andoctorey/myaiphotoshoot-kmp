package ai.create.photo.core.billing

import ai.create.photo.app.App.Companion.context
import ai.create.photo.core.extention.toast
import ai.create.photo.core.log.Warning
import ai.create.photo.ui.settings.balance.Pricing
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import co.touchlab.kermit.Logger
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.billing_service_disconnected
import photocreateai.composeapp.generated.resources.billing_service_internal_error
import photocreateai.composeapp.generated.resources.billing_service_outdated
import photocreateai.composeapp.generated.resources.billing_service_unavailable

enum class Sku(val productId: String, val productType: String, val dollars: Double) {
    TOP_UP_5("top_up_5", BillingClient.ProductType.INAPP, 4.99),
}

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed
 * https://developer.android.com/google/play/billing/billing_library_overview
 */
class BillingRepository(context: Context) : PurchasesUpdatedListener, BillingClientStateListener {

    init {
        Logger.i("Creating BillingRepository")
    }

    private val base64Key =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxVu6XM9IEajbV+MzAB7XtAuBK4iZoyk24ZBUNsWPhYS8yZlbxTUtbFpE7AAp7JQU/9tQeXF+n+VTgRil9qZDn7yPZsX6XJsd2yG1/SQRhNKOd2YAur8DICZj+XIABp0bqZWJbTeHHS1KbPc3rEowTKvpNqye2fAN25jzLqsu/E/yd3nZj2MENcvTDLvZ04qTItlPaEaxJaBNYUYVCYmHd2xG3Cl5lvNj8dMHZVH+nEIqGTPR3ZzLKM084oL2NvHbobpvkpGu8c4YUr5cOXIGhuBl7UoaRgit9QcsvY6hUcBYEAypLtoPcn+unTMkEHH0K678URXhdcpQGrHoCVqkmQIDAQAB"
    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .setListener(this).build()
    private var lastResponseCode = Int.MIN_VALUE
    private var lastResponseMessage = ""
    private var purchaseInProgress = false
    private var processedPurchases = mutableSetOf<String>()
    private var reconnectMilliseconds: Long = 1000
    private var serviceConnected = false
    private var productDetailsList: List<ProductDetails> = listOf()

    fun connectToPlayBillingService() {
        if (!billingClient.isReady) {
            Logger.i("billingClient startConnection")
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Logger.i("onBillingSetupFinished with code ${billingResult.responseCode} and message: ${billingResult.debugMessage}")
        reconnectMilliseconds = 1000
        serviceConnected = true
        lastResponseCode = billingResult.responseCode
        lastResponseMessage = billingResult.debugMessage
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                queryProductDetails()
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                Logger.i(billingResult.debugMessage)
                if (purchaseInProgress) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.toast(getString(Res.string.billing_service_unavailable))
                    }
                }
            }

            else -> Logger.i(billingResult.debugMessage)
        }
    }

    override fun onBillingServiceDisconnected() {
        Logger.i("onBillingServiceDisconnected")
        serviceConnected = false
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (serviceConnected) return@postDelayed
                reconnectMilliseconds *= 2
                connectToPlayBillingService()
            },
            reconnectMilliseconds
        )
    }

    fun endConnection() {
        Logger.i("billingClient endConnection")
        if (!purchaseInProgress && billingClient.isReady) {
            Logger.i("billingClient endConnection successful")
            billingClient.endConnection()
        }
    }

    private fun queryProductDetails() {
        Logger.i("queryProductDetails")
        CoroutineScope(Dispatchers.IO).launch {
            val inApps = Sku.values()
                .filter { it.productType == BillingClient.ProductType.INAPP }
                .map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it.productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            val inAppsParams = QueryProductDetailsParams.newBuilder().setProductList(inApps)
            val response = billingClient.queryProductDetails(inAppsParams.build())
            if (billingResultOk(response.billingResult)) {
                if (response.productDetailsList.isNullOrEmpty()) {
                    Logger.w("No inApps SkuDetailsList on Google Play side. Wrong app package?")
                } else {
                    Logger.v(Json.encodeToString(response.productDetailsList))
                    productDetailsList = response.productDetailsList!!
                }
            }

            queryPurchases()
        }
    }

    private fun billingResultOk(billingResult: BillingResult): Boolean {
        lastResponseCode = billingResult.responseCode
        lastResponseMessage = billingResult.debugMessage
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> return true
            else -> if (billingResult.responseCode !in
                intArrayOf(
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    BillingClient.BillingResponseCode.ERROR,
                    BillingClient.BillingResponseCode.NETWORK_ERROR,
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                )
            ) {
                val error =
                    "debugMessage: " + billingResult.debugMessage + " , code: " + billingResult.responseCode
                Logger.e("billingResultOk failed", Warning(error))
            }
        }
        return false
    }

    fun launchBillingFlow(activity: Activity, pricing: Pricing): Boolean {
        Logger.i("launchBillingFlow for $pricing, productDetailsList size: ${productDetailsList.size}")
        var productDetails: ProductDetails? = null
        for (detail in productDetailsList) {
            if (detail.productId == pricing.productId) {
                productDetails = detail
            }
        }
        if (productDetails == null) {
            Logger.i(
                "productDetails == null",
                Warning("Code: $lastResponseCode, message: $lastResponseMessage")
            )
            CoroutineScope(Dispatchers.Main).launch {
                val toastMessage: String = when (lastResponseCode) {
                    BillingClient.BillingResponseCode.OK,
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                        getString(Res.string.billing_service_unavailable)

                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
                        getString(Res.string.billing_service_outdated)

                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    BillingClient.BillingResponseCode.NETWORK_ERROR ->
                        getString(Res.string.billing_service_disconnected)

                    BillingClient.BillingResponseCode.ERROR ->
                        getString(Res.string.billing_service_internal_error)

                    else -> {
                        Logger.e("launchBillingFlow", Warning("Cannot launch billing flow"))
                        lastResponseMessage
                    }
                }
                context.toast(toastMessage)
            }
            return false
        }

        purchaseInProgress = true
        Handler(Looper.getMainLooper()).postDelayed({ purchaseInProgress = false }, 1200000)
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder().apply {
                setProductDetails(productDetails)
            }.build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
        return true
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Logger.i("onPurchasesUpdated")
                // will handle server verification, consumables, and updating the local cache
                purchases?.apply { processPurchases(this) }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // item already owned? call queryPurchasesAsync to verify and process all such items
                Logger.i("onPurchasesUpdated.ITEM_ALREADY_OWNED: ${billingResult.debugMessage}")
                queryPurchases()
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Logger.i("onPurchasesUpdated: SERVICE_DISCONNECTED")
                connectToPlayBillingService()
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Logger.i("onPurchasesUpdated() - user cancelled the purchase flow - skipping")
            }

            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Logger.i("onPurchasesUpdated() - SERVICE_UNAVAILABLE")
            }

            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                Logger.i("onPurchasesUpdated(): NETWORK_ERROR")
            }

            else -> {
                Logger.i(
                    "onPurchasesUpdated() got unknown resultCode: ${billingResult.responseCode}, " +
                            "debugMessage: ${billingResult.debugMessage}",
                )
            }
        }
    }

    private fun queryPurchases() = CoroutineScope(Dispatchers.IO).launch {
        Logger.i("queryPurchases ")
//        val inAppPurchases = billingClient.queryPurchasesAsync(
//            QueryPurchasesParams.newBuilder()
//                .setProductType(BillingClient.ProductType.INAPP)
//                .build()
//        )
//        processPurchases(inAppPurchases)
    }

    private fun processPurchases(purchasesResult: List<Purchase>) {
        Logger.i("process purchases: $purchasesResult")
        val validPurchases = HashSet<Purchase>(purchasesResult.size)
        purchasesResult.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (isSignatureValid(purchase)) {
                    validPurchases.add(purchase)
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                Logger.i("Received a pending purchase: ${Json.encodeToString(purchase)}")
            } else {
                Logger.i("Received an invalid purchase: ${Json.encodeToString(purchase)}")
            }
        }

        Logger.i("valid purchases: $validPurchases")
        var processingIapPurchase = false
        for (purchase in validPurchases) {
            val orderId = purchase.orderId ?: continue
            if (orderId in processedPurchases) continue
            try {
                when {
                    // TODO
                    Pricing.CREATIVE.productId in purchase.products -> {
                        processingIapPurchase = true
                        Logger.i("consumeAsync")
                        val params =
                            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                                .build()
                        billingClient.consumeAsync(params) { billingResult, _ ->
                            Logger.i("consumeAsync finished with code ${billingResult.responseCode} and message: ${billingResult.debugMessage}")
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                                user.payments.addSmsPackage(userRef, Sku.SMS_2.dollars)
                            } else {
                                processedPurchases.remove(orderId)
                            }
                            purchaseInProgress = false
                        }
                    }
                }
                processedPurchases.add(orderId)
            } catch (e: Exception) {
                Logger.e("processPurchases", e)
                context.toast(e.message)
            }
        }
    }

    /**
     * Ideally your implementation will comprise a secure server, rendering this check
     * unnecessary. @see [Security]
     */
    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(base64Key, purchase.originalJson, purchase.signature)
    }
}