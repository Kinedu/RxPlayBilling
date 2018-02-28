package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient

sealed class ConnectionResult {
    object Success : ConnectionResult()
    object Disconnected : ConnectionResult()
    data class Failure(
        @BillingClient.BillingResponse val billingResponse: Int
    ) : ConnectionResult()
}
