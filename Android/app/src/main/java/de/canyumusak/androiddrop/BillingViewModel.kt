package de.canyumusak.androiddrop

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BillingViewModel(application: Application)
    : AndroidViewModel(application),
        PurchasesUpdatedListener,
        BillingClientStateListener,
        SkuDetailsResponseListener {

    val billingConnectionState = MutableLiveData<BillingConnectionState>().also {
        it.value = BillingConnectionState.Connecting
    }

    val skuDetails = MutableLiveData<Map<Tip, Details>>()
    val tippingSum = MutableLiveData<TippingSum>()

    val purchaseResult = MutableLiveData<PurchaseEvent>()

    private val viewModelScope = CoroutineScope(Job() + Dispatchers.Main)

    private val billingClient = BillingClient
            .newBuilder(application.applicationContext)
            .setListener(this)
            .build()

    init {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        Log.d("BillingViewModel", "Billing setup finished with response $responseCode")

        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                billingConnectionState.value = BillingConnectionState.GettingDetails
                querySkuDetails()
            }
            BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponse.SERVICE_DISCONNECTED,
            BillingClient.BillingResponse.USER_CANCELED,
            BillingClient.BillingResponse.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponse.BILLING_UNAVAILABLE,
            BillingClient.BillingResponse.ITEM_UNAVAILABLE,
            BillingClient.BillingResponse.DEVELOPER_ERROR,
            BillingClient.BillingResponse.ERROR,
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponse.ITEM_NOT_OWNED -> {
                billingConnectionState.value = BillingConnectionState.Failed
            }
        }
    }

    private fun updatePurchaseHistory() = viewModelScope.launch {
//        this does not work as expected
//        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { responseCode: Int, purchasesList: List<Purchase> ->
//            when (responseCode) {
//                BillingClient.BillingResponse.OK -> {
//                    purchasesList.forEach {
//                        billingClient.consumeAsync(it.purchaseToken) { _, _ -> }
//                    }
//
//                    val tips = purchasesList.map { purchase ->
//                        Tip.fromSku(purchase.sku)
//                    }
//
//                    val sumOfTips = tips.mapNotNull {
//                        skuDetails.value?.get(it)
//                    }.sumByDouble {
//                        it.skuDetails.priceAmountMicros.toDouble() / 1_000_000
//                    }
//
//                    tippingSum.value = if (tips.isEmpty()) {
//                        TippingSum.NoTips
//                    } else {
//                        skuDetails.value?.get(Tip.Big)?.currencyCode?.let {
//                            val formattedSum = "%.2f".format(sumOfTips) + " $it"
//                            TippingSum.Suceeded(formattedSum)
//                        } ?: TippingSum.FailedToLoad
//                    }
//                }
//                BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED,
//                BillingClient.BillingResponse.SERVICE_DISCONNECTED,
//                BillingClient.BillingResponse.USER_CANCELED,
//                BillingClient.BillingResponse.SERVICE_UNAVAILABLE,
//                BillingClient.BillingResponse.BILLING_UNAVAILABLE,
//                BillingClient.BillingResponse.ITEM_UNAVAILABLE,
//                BillingClient.BillingResponse.DEVELOPER_ERROR,
//                BillingClient.BillingResponse.ERROR,
//                BillingClient.BillingResponse.ITEM_ALREADY_OWNED,
//                BillingClient.BillingResponse.ITEM_NOT_OWNED -> {
//                    tippingSum.value = TippingSum.FailedToLoad
//                }
//            }
//        }
    }

    private fun querySkuDetails() = viewModelScope.launch {
        Log.d("BillingViewModel", "Querying sku details")

        val queryParams = SkuDetailsParams
                .newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(Tip.values.map { it.id })
                .build()

        billingClient.querySkuDetailsAsync(queryParams, this@BillingViewModel)
    }

    override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
        Log.d("BillingViewModel", "Received Sku Details Response $responseCode")

        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                billingConnectionState.value = BillingConnectionState.Connected
                skuDetails.value = skuDetailsList?.asPriceMap()
                updatePurchaseHistory()
            }
            BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponse.SERVICE_DISCONNECTED,
            BillingClient.BillingResponse.USER_CANCELED,
            BillingClient.BillingResponse.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponse.BILLING_UNAVAILABLE,
            BillingClient.BillingResponse.ITEM_UNAVAILABLE,
            BillingClient.BillingResponse.DEVELOPER_ERROR,
            BillingClient.BillingResponse.ERROR,
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponse.ITEM_NOT_OWNED -> {
                billingConnectionState.value = BillingConnectionState.Failed
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d("BillingViewModel", "Billing Service disconnected")

        billingConnectionState.value = BillingConnectionState.Failed
    }

    fun buyTip(activity: Activity, tip: Tip) {
        skuDetails.value?.get(tip)?.let { details ->
            val purchaseParams = BillingFlowParams
                    .newBuilder()
                    .setSkuDetails(details.skuDetails)
                    .build()

            billingClient.launchBillingFlow(activity, purchaseParams)
        } ?: run {
            purchaseResult.value = PurchaseEvent(PurchaseResult.Fail, tip)
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        val purchase = purchases?.firstOrNull()
        val tip = Tip.fromSku(purchase?.sku)

        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                purchaseResult.value = tip?.let {
                    purchase?.purchaseToken?.let { token ->
                        billingClient.consumeAsync(token) { _, _ -> }
                    }
                    PurchaseEvent(PurchaseResult.Success, it)
                } ?: PurchaseEvent(PurchaseResult.Fail, null)

                updatePurchaseHistory()
            }
            BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponse.SERVICE_DISCONNECTED,
            BillingClient.BillingResponse.USER_CANCELED,
            BillingClient.BillingResponse.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponse.BILLING_UNAVAILABLE,
            BillingClient.BillingResponse.ITEM_UNAVAILABLE,
            BillingClient.BillingResponse.DEVELOPER_ERROR,
            BillingClient.BillingResponse.ERROR,
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponse.ITEM_NOT_OWNED -> {
                purchaseResult.value = PurchaseEvent(PurchaseResult.Fail, tip)
            }
        }
    }

    private fun MutableList<SkuDetails>.asPriceMap(): Map<Tip, Details> {
        return mapNotNull { skuDetail ->
            val tip = Tip.fromSku(skuDetail.sku)
            val price = Details(skuDetail.description, skuDetail.price, skuDetail.priceCurrencyCode, skuDetail)
            tip?.let { it to price }
        }.toMap()
    }
}

data class Details(val title: String, val price: String, val currencyCode: String, val skuDetails: SkuDetails)

sealed class Tip(val id: String) {
    object Small : Tip("small_tip")
    object Medium : Tip("medium_tipp")
    object Big : Tip("big_tip")

    companion object {
        val values = listOf(Small, Medium, Big)

        fun fromSku(sku: String?): Tip? {
            return Tip.values.firstOrNull { sku == it.id }
        }
    }
}

sealed class BillingConnectionState {
    object Connected : BillingConnectionState()
    object GettingDetails : BillingConnectionState()
    object Failed : BillingConnectionState()
    object Connecting : BillingConnectionState()
}

class PurchaseEvent(val result: PurchaseResult, val tip: Tip?)
sealed class PurchaseResult {
    object Success : PurchaseResult()
    object Fail : PurchaseResult()
}

sealed class TippingSum {
    object FailedToLoad : TippingSum()
    class Suceeded(val sum: String) : TippingSum()
    object NoTips : TippingSum()
}