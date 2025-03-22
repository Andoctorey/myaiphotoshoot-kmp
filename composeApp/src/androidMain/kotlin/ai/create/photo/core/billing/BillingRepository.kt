package ai.create.photo.core.billing

import ai.create.photo.app.App
import ai.create.photo.core.extention.toast
import ai.create.photo.core.log.Warning
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.ui.settings.balance.Pricing
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.billing_service_unavailable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@SuppressLint("StaticFieldLeak")
object BillingRepository : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient

    private const val API_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxVu6XM9IEajbV+MzAB7XtAuBK4iZoyk24ZBUNsWPhYS8yZlbxTUtbFpE7AAp7JQU/9tQeXF+n+VTgRil9qZDn7yPZsX6XJsd2yG1/SQRhNKOd2YAur8DICZj+XIABp0bqZWJbTeHHS1KbPc3rEowTKvpNqye2fAN25jzLqsu/E/yd3nZj2MENcvTDLvZ04qTItlPaEaxJaBNYUYVCYmHd2xG3Cl5lvNj8dMHZVH+nEIqGTPR3ZzLKM084oL2NvHbobpvkpGu8c4YUr5cOXIGhuBl7UoaRgit9QcsvY6hUcBYEAypLtoPcn+unTMkEHH0K678URXhdcpQGrHoCVqkmQIDAQAB"
    private var processedPurchases = mutableSetOf<String>()
    private var productDetailsList: List<ProductDetails> = listOf()

    private fun initBillingClient(context: Context) {
        if (!::billingClient.isInitialized) {
            billingClient = BillingClient.newBuilder(context.applicationContext)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .setListener(this)
                .build()
        }
    }

    suspend fun purchase(activity: Activity, pricing: Pricing) = runCatching {
        initBillingClient(activity.applicationContext)
        val billingResult = billingClient.connect()
        queryProductDetails()
        queryPurchases()
        Logger.i("billingClient.connect() result: code ${billingResult.responseCode}, message: ${billingResult.debugMessage}")
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            val error =
                "debugMessage: ${billingResult.debugMessage}, code: ${billingResult.responseCode}"
            Logger.e("billingResultOk failed", Warning(error))
            throw Exception(getString(Res.string.billing_service_unavailable))
        }

        Logger.i("startBillingFlow for $pricing, productDetailsList size: ${productDetailsList.size}")
        openBillingPopup(activity, pricing)
    }

    private suspend fun queryProductDetails() {
        val inApps = Pricing.entries.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val response = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder().setProductList(inApps).build()
        )
        val billingResult = response.billingResult
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            val error =
                "debugMessage: ${billingResult.debugMessage}, code: ${billingResult.responseCode}"
            Logger.w("billingResultOk failed", Warning(error))
            throw Exception(getString(Res.string.billing_service_unavailable))
        }
        productDetailsList = response.productDetailsList ?: emptyList()
    }

    private suspend fun openBillingPopup(activity: Activity, pricing: Pricing) {
        val productDetails = productDetailsList.find { it.productId == pricing.productId }
            ?: throw Exception(getString(Res.string.billing_service_unavailable))
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Logger.i("onPurchasesUpdated")
                purchases?.apply {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            processPurchases(this@apply)
                        } catch (e: Exception) {
                            Logger.e("onPurchasesUpdated", e)
                            withContext(Dispatchers.Main) {
                                App.context.toast(getString(Res.string.billing_service_unavailable))
                            }
                        }
                    }
                }
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Logger.i("onPurchasesUpdated.ITEM_ALREADY_OWNED: ${billingResult.debugMessage}")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        queryPurchases() // try to consume again
                    } catch (e: Exception) {
                        Logger.e("onPurchasesUpdated", e)
                        withContext(Dispatchers.Main) {
                            App.context.toast(getString(Res.string.billing_service_unavailable))
                        }
                    }
                }
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                Logger.i("onPurchasesUpdated: SERVICE_DISCONNECTED")
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

    private suspend fun queryPurchases() {
        val response = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val billingResult = response.billingResult
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            val error =
                "debugMessage: ${billingResult.debugMessage}, code: ${billingResult.responseCode}"
            Logger.e("billingResultOk failed", Warning(error))
            throw Exception(getString(Res.string.billing_service_unavailable))
        }
        processPurchases(response.purchasesList)
    }

    private suspend fun processPurchases(purchasesResult: List<Purchase>) {
        Logger.i("process purchases: $purchasesResult")
        val validPurchases = purchasesResult.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED && isSignatureValid(it)
        }.toHashSet()

        Logger.i("valid purchases: $validPurchases")
        for (purchase in validPurchases) {
            val orderId = purchase.orderId ?: continue
            if (orderId in processedPurchases) continue
            for (product in purchase.products) {
                val verified = SupabaseFunction.verifyAndroidPurchase(
                    productId = product,
                    purchaseToken = purchase.purchaseToken,
                )
                if (!verified) {
                    Logger.e("processPurchases", Warning("Failed to verify purchase: $product"))
                }
            }
            val params =
                ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            val result = billingClient.consumePurchase(params).billingResult
            Logger.i("consumeAsync finished with code ${result.responseCode} and message: ${result.debugMessage}")
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processedPurchases.add(orderId)
            } else {
                Logger.e("Consumption failed for $orderId", Warning(result.debugMessage))
            }
        }
        billingClient.endConnection()
    }

    private fun isSignatureValid(purchase: Purchase) =
        Security.verifyPurchase(API_KEY, purchase.originalJson, purchase.signature)
}

suspend fun BillingClient.connect(): BillingResult = suspendCoroutine { continuation ->
    startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            continuation.resume(billingResult)
        }

        override fun onBillingServiceDisconnected() {
            continuation.resumeWithException(Exception("Service disconnected"))
        }
    })
}