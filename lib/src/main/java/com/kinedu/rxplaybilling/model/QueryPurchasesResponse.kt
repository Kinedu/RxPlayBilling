package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

sealed class QueryPurchasesResponse {
    data class Success(val purchaseList: List<Purchase>) : QueryPurchasesResponse()
    data class Failure(
        @BillingClient.BillingResponse val billingResponse: Int
    ) : QueryPurchasesResponse()
}
