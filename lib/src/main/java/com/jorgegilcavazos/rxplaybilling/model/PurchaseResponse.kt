package com.jorgegilcavazos.rxplaybilling.model

import com.android.billingclient.api.BillingClient

sealed class PurchaseResponse {
    object Success : PurchaseResponse()
    data class Failure(@BillingClient.BillingResponse val billingResponse: Int) : PurchaseResponse()
}
