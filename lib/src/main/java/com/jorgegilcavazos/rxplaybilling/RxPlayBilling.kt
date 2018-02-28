package com.jorgegilcavazos.rxplaybilling

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import com.jorgegilcavazos.rxplaybilling.model.ConnectionResult
import com.jorgegilcavazos.rxplaybilling.model.ConsumptionResponse
import com.jorgegilcavazos.rxplaybilling.model.PurchaseResponse
import com.jorgegilcavazos.rxplaybilling.model.PurchasesUpdatedResponse
import com.jorgegilcavazos.rxplaybilling.model.QueryPurchasesResponse
import com.jorgegilcavazos.rxplaybilling.model.SkuDetailsResponse

/**
 * RxJava wrapper for the Play Billing Client library.
 * https://developer.android.com/google/play/billing/billing_library.html
 *
 * It provides convenience methods for in-app billing using observable sequences. Upon creation of
 * an instance of this class you must call [connect] and only after receiving a
 * [ConnectionResult.Success] you may start calling other methods.
 *
 * When you are done using this object, call [endConnection] to ensure proper cleanup.
 */
class RxPlayBilling constructor(context: Context) : PurchasesUpdatedListener {

    private val billingClient: BillingClient =
        BillingClient
            .newBuilder(context)
            .setListener(this)
            .build()

    private val purchasesUpdates = PublishSubject.create<PurchasesUpdatedResponse>()

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            purchasesUpdates.onNext(PurchasesUpdatedResponse.Success(purchases ?: listOf()))
        } else {
            purchasesUpdates.onNext(PurchasesUpdatedResponse.Failure(responseCode))
        }
    }

    /**
     * Returns true if the client is currently connected to the service. False otherwise.
     */
    fun isReady(): Boolean = billingClient.isReady

    /**
     * Starts the billing client setup.
     *
     * @return an Observable that emits:
     *  - [ConnectionResult.Success] when the billing client's setup finishes with
     *  [BillingClient.BillingResponse.OK] or [ConnectionResult.Failure] otherwise.
     *  - [ConnectionResult.Disconnected] when the service is disconnected.
     *
     */
    fun connect(): Observable<ConnectionResult> {
        return Observable.create {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(
                    @BillingClient.BillingResponse responseCode: Int
                ) {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        it.onNext(ConnectionResult.Success)
                    } else {
                        it.onNext(ConnectionResult.Failure(responseCode))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    it.onNext(ConnectionResult.Disconnected)
                }
            })
        }
    }

    /**
     * Closes the client's connection and releases all held resources.
     */
    fun endConnection() {
        billingClient.endConnection()
    }

    /**
     * Returns an Observable that emits [PurchasesUpdatedResponse.Success] any time the client
     * receives a purchase update with response code [BillingClient.BillingResponse.OK] or
     * emits [PurchasesUpdatedResponse.Failure] otherwise.
     */
    fun purchasesUpdates(): Observable<PurchasesUpdatedResponse> {
        return purchasesUpdates
    }

    /**
     * Returns a Single of the purchase details for all in-app items bought within your app. The
     * returned details are obtained from a local cache.
     *
     * Use [queryInAppSkuDetails] to force a network call for sku details.
     */
    fun queryInAppPurchases(): Single<QueryPurchasesResponse> {
        return Single.create {
            val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            if (result.responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(QueryPurchasesResponse.Success(result.purchasesList))
            } else {
                it.onSuccess(QueryPurchasesResponse.Failure(result.responseCode))
            }
        }
    }

    /**
     * Returns a Single of the purchase details for all subscription items bought within your app.
     * The returned details are obtained from a local cache.
     *
     * Use [querySubscriptionsSkuDetails] to force a network call for sku details.
     */
    fun querySubscriptionPurchases(): Single<QueryPurchasesResponse> {
        return Single.create {
            val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
            if (result.responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(QueryPurchasesResponse.Success(result.purchasesList))
            } else {
                it.onSuccess(QueryPurchasesResponse.Failure(result.responseCode))
            }
        }
    }

    /**
     * Returns a Single of the purchase details for the given list of in-app items' SKUs.
     *
     * @param skuList a list of in-app SKUs to query the details of
     * @return a Single that emits [SkuDetailsResponse.Success] when the query's response code is
     * [BillingClient.BillingResponse.OK], or [SkuDetailsResponse.Failure] otherwise
     */
    fun queryInAppSkuDetails(skuList: List<String>): Single<SkuDetailsResponse> {
        return Single.create {
            val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(SkuDetailsResponse.Success(skuDetailsList ?: listOf()))
                } else {
                    it.onSuccess(SkuDetailsResponse.Failure(responseCode))
                }
            }
        }
    }

    /**
     * Returns a Single of the purchase details for the given list of subscription items' SKUs.
     *
     * @param skuList a list of subscription SKUs to query the details of
     * @return a Single that emits [SkuDetailsResponse.Success] when the query's response code is
     * [BillingClient.BillingResponse.OK], or [SkuDetailsResponse.Failure] otherwise
     */
    fun querySubscriptionsSkuDetails(skuList: List<String>): Single<SkuDetailsResponse> {
        return Single.create {
            val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.SUBS)
                .build()
            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(SkuDetailsResponse.Success(skuDetailsList ?: listOf()))
                } else {
                    it.onSuccess(SkuDetailsResponse.Failure(responseCode))
                }
            }
        }
    }

    /**
     * Returns a Single of the most recent in-app purchase made by the user for each SKU, even if
     * that purchase is expired, canceled, or consumed.
     */
    fun queryInAppPurchaseHistory(): Single<QueryPurchasesResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) {
                responseCode, purchasesList ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(QueryPurchasesResponse.Success(purchasesList ?: listOf()))
                } else {
                    it.onSuccess(QueryPurchasesResponse.Failure(responseCode))
                }
            }
        }
    }

    /**
     * Returns a Single of the most recent subscription purchase made by the user for each SKU,
     * even if that purchase is expired, canceled, or consumed.
     */
    fun querySubscriptionPurchaseHistory(): Single<QueryPurchasesResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) {
                responseCode, purchasesList ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(QueryPurchasesResponse.Success(purchasesList ?: listOf()))
                } else {
                    it.onSuccess(QueryPurchasesResponse.Failure(responseCode))
                }

            }
        }
    }

    /**
     * Consumes a given in-app product. Consuming can only be done on an item that's owned, and as a
     * result of consumption, the user will no longer own it.
     *
     * @param purchaseToken the purchase token of the item to consume
     * @return a Single that emits [ConsumptionResponse.Success] when the consumption operation
     * succeeds with [BillingClient.BillingResponse.OK], or [ConsumptionResponse.Failure] otherwise.
     */
    fun consumeItem(purchaseToken: String): Single<ConsumptionResponse> {
        return Single.create {
            billingClient.consumeAsync(purchaseToken) { responseCode, outToken ->
                if (responseCode == BillingClient.BillingResponse.OK) {
                    it.onSuccess(ConsumptionResponse.Success(outToken))
                } else {
                    it.onSuccess(ConsumptionResponse.Failure(responseCode))
                }
            }
        }
    }

    /**
     * Initiates the billing flow for an in-app purchase.
     *
     * @param skuId the sku that is being purchased
     * @param activity an activity reference from which the billing flow will be launched
     * @return a Single that emits [PurchaseResponse.Success] if the the flow was launched with
     * response code [BillingClient.BillingResponse.OK], or [PurchaseResponse.Failure] otherwise.
     */
    fun purchaseItem(skuId: String, activity: Activity): Single<PurchaseResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            if (responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(PurchaseResponse.Success)
            } else {
                it.onSuccess(PurchaseResponse.Failure(responseCode))
            }
        }
    }

    /**
     * Initiates the billing flow for a subscription purchase.
     *
     * @param skuId the sku that is being purchased
     * @param activity an activity reference from which the billing flow will be launched
     * @return a Single that emits [PurchaseResponse.Success] if the the flow was launched with
     * response code [BillingClient.BillingResponse.OK], or [PurchaseResponse.Failure] otherwise.
     */
    fun purchaseSubscription(skuId: String, activity: Activity): Single<PurchaseResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.SUBS)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            if (responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(PurchaseResponse.Success)
            } else {
                it.onSuccess(PurchaseResponse.Failure(responseCode))
            }
        }
    }

    /**
     * Initiates the billing flow for a subscription upgrade/downgrade.
     *
     * @param oldSkuId the sku that the user is upgrading or downgrading from
     * @param newSkuId the sku that is being upgraded or downgraded to
     * @param activity an activity reference from which the billing flow will be launched
     * @return a Single that emits [PurchaseResponse.Success] if the the flow was launched with
     * response code [BillingClient.BillingResponse.OK], or [PurchaseResponse.Failure] otherwise.
     */
    fun replaceSubscription(
        oldSkuId: String,
        newSkuId: String,
        activity: Activity
    ): Single<PurchaseResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .addOldSku(oldSkuId)
                    .setSku(newSkuId)
                    .setType(BillingClient.SkuType.SUBS)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
            if (responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(PurchaseResponse.Success)
            } else {
                it.onSuccess(PurchaseResponse.Failure(responseCode))
            }
        }
    }
}
