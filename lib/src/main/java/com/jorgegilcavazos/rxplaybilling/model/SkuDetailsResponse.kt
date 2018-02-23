package com.jorgegilcavazos.rxplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails

sealed class SkuDetailsResponse {
    data class Success(val skuDetailsList: List<SkuDetails>) : SkuDetailsResponse()
    data class Failure(
        @BillingClient.BillingResponse val billingResponse: Int
    ) : SkuDetailsResponse()
}
