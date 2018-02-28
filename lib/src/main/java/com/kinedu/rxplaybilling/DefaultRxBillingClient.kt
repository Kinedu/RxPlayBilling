package com.kinedu.rxplaybilling

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.kinedu.rxplaybilling.model.ConnectionResult
import com.kinedu.rxplaybilling.model.ConsumptionResponse
import com.kinedu.rxplaybilling.model.PurchaseResponse
import com.kinedu.rxplaybilling.model.PurchasesUpdatedResponse
import com.kinedu.rxplaybilling.model.QueryPurchasesResponse
import com.kinedu.rxplaybilling.model.SkuDetailsResponse
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

/**
 * Default implementation of the [RxBillingClient] interface. It is backed by the [BillingClient]
 * class of the Play Billing Library.
 */
class DefaultRxBillingClient constructor(
    context: Context
) : RxBillingClient, PurchasesUpdatedListener {

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

    override fun isReady(): Boolean = billingClient.isReady

    override fun connect(): Observable<ConnectionResult> {
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

    override fun endConnection() {
        billingClient.endConnection()
    }

    override fun purchasesUpdates(): Observable<PurchasesUpdatedResponse> {
        return purchasesUpdates
    }

    override fun queryInAppPurchases(): Single<QueryPurchasesResponse> {
        return Single.create {
            val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            if (result.responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(QueryPurchasesResponse.Success(result.purchasesList))
            } else {
                it.onSuccess(QueryPurchasesResponse.Failure(result.responseCode))
            }
        }
    }

    override fun querySubscriptionPurchases(): Single<QueryPurchasesResponse> {
        return Single.create {
            val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
            if (result.responseCode == BillingClient.BillingResponse.OK) {
                it.onSuccess(QueryPurchasesResponse.Success(result.purchasesList))
            } else {
                it.onSuccess(QueryPurchasesResponse.Failure(result.responseCode))
            }
        }
    }

    override fun queryInAppSkuDetails(skuList: List<String>): Single<SkuDetailsResponse> {
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

    override fun querySubscriptionsSkuDetails(skuList: List<String>): Single<SkuDetailsResponse> {
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

    override fun queryInAppPurchaseHistory(): Single<QueryPurchasesResponse> {
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

    override fun querySubscriptionPurchaseHistory(): Single<QueryPurchasesResponse> {
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

    override fun consumeItem(purchaseToken: String): Single<ConsumptionResponse> {
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

    override fun purchaseItem(skuId: String, activity: Activity): Single<PurchaseResponse> {
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

    override fun purchaseSubscription(skuId: String, activity: Activity): Single<PurchaseResponse> {
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

    override fun replaceSubscription(
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
