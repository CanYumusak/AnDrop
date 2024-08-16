package de.canyumusak.androiddrop

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingViewModel(application: Application) : AndroidViewModel(application),
    PurchasesUpdatedListener,
    BillingClientStateListener {

    private val _billingConnectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Connecting)
    val billingConnectionState = _billingConnectionState.asStateFlow()

    private val _skuViewDetails = MutableStateFlow<Map<Tip, Details>?>(null)
    val skuViewDetails = _skuViewDetails.asStateFlow()

    private var skuDetails: Map<Tip, ProductDetails> = emptyMap()

    private val _tippingSum = MutableStateFlow<TippingSum>(TippingSum.NotYetLoaded)
    val tippingSum = _tippingSum.asStateFlow()

    private val purchaseChannel = Channel<PurchaseEvent>(Channel.Factory.UNLIMITED)

    private val billingClient = BillingClient
        .newBuilder(application.applicationContext)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    init {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        Log.d("BillingViewModel", "Billing setup finished with response $responseCode")

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                _billingConnectionState.value = BillingConnectionState.GettingDetails
                querySkuDetails()
            }

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                _billingConnectionState.value = BillingConnectionState.Failed
            }
        }
    }

    private fun querySkuDetails() = viewModelScope.launch {
        Log.d("BillingViewModel", "Querying sku details")

        val products = Tip.values()
            .map { tip ->
                tip.asProductQueryParam()
            }
        val queryParams = QueryProductDetailsParams
            .newBuilder()
            .setProductList(products)
            .build()

        billingClient.queryProductDetailsAsync(queryParams) { result, productDetails ->
            onSkuDetailsResponse(result, productDetails)
        }
    }

    private fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: List<ProductDetails>) {
        val responseCode = billingResult.responseCode
        Log.d("BillingViewModel", "Received Sku Details Response $responseCode")

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                _billingConnectionState.value = BillingConnectionState.Connected
                _skuViewDetails.value = skuDetailsList.asPriceMap()
                skuDetails = skuDetailsList.mapNotNull { skuDetail ->
                    val tip = Tip.fromId(skuDetail.productId)
                    tip?.let { it to skuDetail }
                }.toMap()
            }

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                _billingConnectionState.value = BillingConnectionState.Failed
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d("BillingViewModel", "Billing Service disconnected")
        _billingConnectionState.value = BillingConnectionState.Failed
    }

    suspend fun buyTip(activity: Activity, tip: Tip): PurchaseEvent {
        skuDetails[tip]?.let { details ->
            val productDetails = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()

            val purchaseParams = BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(listOf(productDetails))
                .build()

            billingClient.launchBillingFlow(activity, purchaseParams)
        } ?: run {
            purchaseChannel.trySend(PurchaseEvent(PurchaseResult.Fail, tip))
        }

        return purchaseChannel.receive()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val purchase = purchases?.firstOrNull()
        val tip = purchase?.skus?.firstOrNull()?.let { Tip.fromId(it) }
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val value = tip?.let {
                    purchase.purchaseToken.let { token ->
                        val params = ConsumeParams.newBuilder().setPurchaseToken(token).build()
                        billingClient.consumeAsync(params) { _, _ -> }
                    }
                    PurchaseEvent(PurchaseResult.Success, it)
                } ?: PurchaseEvent(PurchaseResult.Fail, null)

                purchaseChannel.trySend(value)

//                updatePurchaseHistory()
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseChannel.trySend(PurchaseEvent(PurchaseResult.Cancelled, tip))
            }

            BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                purchaseChannel.trySend(PurchaseEvent(PurchaseResult.Fail, tip))
            }
        }
    }

    private fun List<ProductDetails>.asPriceMap(): Map<Tip, Details> {
        return mapNotNull { skuDetail ->
            val tip = Tip.fromId(skuDetail.productId)
            val price = Details(
                skuDetail.description,
                skuDetail.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
            )
            tip?.let { it to price }
        }.toMap()
    }

    private fun Tip.asProductQueryParam(): QueryProductDetailsParams.Product {
        return QueryProductDetailsParams.Product
            .newBuilder()
            .setProductId(id)
            .setProductType(ProductType.INAPP)
            .build()
    }
}

data class Details(val title: String, val price: String)

enum class Tip(val id: String) {
    Small("small_tip"),
    Medium("medium_tipp"),
    Big("big_tip");

    companion object {
        fun fromId(id: String): Tip? {
            return values().firstOrNull { id == it.id }
        }
    }
}

sealed class BillingConnectionState {
    data object Connected : BillingConnectionState()
    data object GettingDetails : BillingConnectionState()
    data object Failed : BillingConnectionState()
    data object Connecting : BillingConnectionState()
}

class PurchaseEvent(val result: PurchaseResult, val tip: Tip?)

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Fail : PurchaseResult()
    data object Cancelled : PurchaseResult()
}

sealed class TippingSum {
    data object NotYetLoaded : TippingSum()
    data object FailedToLoad : TippingSum()
    class Succeeded(val sum: String) : TippingSum()
    data object NoTips : TippingSum()
}