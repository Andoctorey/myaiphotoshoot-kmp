package ai.create.photo.core.billing

import ai.create.photo.app.App
import ai.create.photo.core.extention.toast
import ai.create.photo.core.log.Warning
import ai.create.photo.ui.settings.balance.Pricing
import android.annotation.SuppressLint
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
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
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

@SuppressLint("StaticFieldLeak")
object BillingRepository : PurchasesUpdatedListener, BillingClientStateListener {

    init {
        Logger.i("Creating BillingRepository")
    }

    private lateinit var billingClient: BillingClient

    private val base64Key =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxVu6XM9IEajbV+MzAB7XtAuBK4iZoyk24ZBUNsWPhYS8yZlbxTUtbFpE7AAp7JQU/9tQeXF+n+VTgRil9qZDn7yPZsX6XJsd2yG1/SQRhNKOd2YAur8DICZj+XIABp0bqZWJbTeHHS1KbPc3rEowTKvpNqye2fAN25jzLqsu/E/yd3nZj2MENcvTDLvZ04qTItlPaEaxJaBNYUYVCYmHd2xG3Cl5lvNj8dMHZVH+nEIqGTPR3ZzLKM084oL2NvHbobpvkpGu8c4YUr5cOXIGhuBl7UoaRgit9QcsvY6hUcBYEAypLtoPcn+unTMkEHH0K678URXhdcpQGrHoCVqkmQIDAQAB"
    private var lastResponseCode = Int.MIN_VALUE
    private var lastResponseMessage = ""
    private var purchaseInProgress = false
    private var processedPurchases = mutableSetOf<String>()
    private var reconnectMilliseconds: Long = 1000
    private var serviceConnected = false
    private var productDetailsList: List<ProductDetails> = listOf()
    private var pendingLaunchActivity: Activity? = null
    private var pendingLaunchPricing: Pricing? = null
    private val maxReconnectDelayMs = 30_000L

    private fun init(context: Context) {
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener(this)
            .build()
    }

    fun launchBillingFlow(activity: Activity, pricing: Pricing): Boolean {
        if (!::billingClient.isInitialized) {
            init(activity.applicationContext)
        }
        Logger.i("startBillingFlow for $pricing, productDetailsList size: ${productDetailsList.size}")
        if (billingClient.isReady && productDetailsList.isNotEmpty()) {
            return launchFlow(activity, pricing)
        } else {
            pendingLaunchActivity = activity
            pendingLaunchPricing = pricing
            if (!billingClient.isReady) {
                Logger.i("Billing client not ready, connecting...")
                connectToPlayBillingService()
            } else {
                Logger.i("Billing client ready, querying product details...")
                queryProductDetails()
            }
            return false
        }
    }

    private fun connectToPlayBillingService() {
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
                        App.context.toast(getString(Res.string.billing_service_unavailable))
                    }
                }
            }
            else -> Logger.i(billingResult.debugMessage)
        }
    }

    override fun onBillingServiceDisconnected() {
        serviceConnected = false
        Handler(Looper.getMainLooper()).postDelayed({
            if (serviceConnected) return@postDelayed
            reconnectMilliseconds = minOf(reconnectMilliseconds * 2, maxReconnectDelayMs)
            connectToPlayBillingService()
            queryProductDetails(forceRefresh = true)
        }, reconnectMilliseconds)
    }

    fun endConnection() {
        Logger.i("billingClient endConnection")
        if (!purchaseInProgress && billingClient.isReady) {
            Logger.i("billingClient endConnection successful")
            billingClient.endConnection()
        }
    }

    private fun queryProductDetails(forceRefresh: Boolean = false) {
        if (productDetailsList.isNotEmpty() && !forceRefresh) {
            tryLaunchPendingFlow()
            queryPurchases()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val inApps = Pricing.entries.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it.productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
            val response = billingClient.queryProductDetails(
                QueryProductDetailsParams.newBuilder().setProductList(inApps).build()
            )
            if (billingResultOk(response.billingResult)) {
                productDetailsList = response.productDetailsList ?: emptyList()
            }
            tryLaunchPendingFlow()
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
                    "debugMessage: ${billingResult.debugMessage}, code: ${billingResult.responseCode}"
                Logger.e("billingResultOk failed", Warning(error))
            }
        }
        return false
    }

    private fun launchFlow(activity: Activity, pricing: Pricing): Boolean {
        var productDetails: ProductDetails? = null
        for (detail in productDetailsList) {
            if (detail.productId == pricing.productId) {
                productDetails = detail
                break
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
                        Logger.e("launchFlow", Warning("Cannot launch billing flow"))
                        lastResponseMessage
                    }
                }
                App.context.toast(toastMessage)
            }
            return false
        }
        purchaseInProgress = true
        Handler(Looper.getMainLooper()).postDelayed({ purchaseInProgress = false }, 1200000)
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
        return true
    }

    private fun tryLaunchPendingFlow() {
        val activity = pendingLaunchActivity
        val pricing = pendingLaunchPricing
        if (activity != null && pricing != null && billingClient.isReady && productDetailsList.isNotEmpty()) {
            Logger.i("Launching pending billing flow for $pricing")
            launchFlow(activity, pricing)
            pendingLaunchActivity = null
            pendingLaunchPricing = null
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Logger.i("onPurchasesUpdated")
                purchases?.apply { processPurchases(this) }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
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
                Logger.i("onPurchasesUpdated() got unknown resultCode: ${billingResult.responseCode}, debugMessage: ${billingResult.debugMessage}")
            }
        }
    }

    private fun queryPurchases() = CoroutineScope(Dispatchers.IO).launch {
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (billingResultOk(result.billingResult)) {
            processPurchases(result.purchasesList)
        }
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
                                // Handle successful consumption
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
                App.context.toast(e.message)
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(base64Key, purchase.originalJson, purchase.signature)
    }
}