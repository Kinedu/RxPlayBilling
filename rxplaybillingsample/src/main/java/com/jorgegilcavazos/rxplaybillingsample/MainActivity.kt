package com.jorgegilcavazos.rxplaybillingsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.jorgegilcavazos.rxplaybilling.RxPlayBilling
import com.jorgegilcavazos.rxplaybilling.model.ConnectionResult
import com.jorgegilcavazos.rxplaybilling.model.ConsumptionResponse
import com.jorgegilcavazos.rxplaybilling.model.PurchaseResponse
import com.jorgegilcavazos.rxplaybilling.model.PurchasesUpdatedResponse
import com.jorgegilcavazos.rxplaybilling.model.QueryPurchasesResponse
import com.jorgegilcavazos.rxplaybilling.model.SkuDetailsResponse

class MainActivity : AppCompatActivity() {

  lateinit var rxPlayBilling: RxPlayBilling

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    rxPlayBilling = RxPlayBilling(this)

    rxPlayBilling.connect()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ connectionResult ->
          when (connectionResult) {
            ConnectionResult.Success -> {
              // The billing client is ready. You can now query purchases.
            }
            is ConnectionResult.Failure -> {
              when (connectionResult.billingResponse) {
                BillingClient.BillingResponse.DEVELOPER_ERROR -> {
                  // Handle an error caused by the developer.
                }
                else -> {
                  // Handle any other error codes.
                }
              }
            }
            ConnectionResult.Disconnected -> {
              // The billing client got disconnected, handle or attempt to reconnect.
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.purchasesUpdates()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ purchasesUpdatedResponse ->
          when (purchasesUpdatedResponse) {
            is PurchasesUpdatedResponse.Success -> {
              val purchases = purchasesUpdatedResponse.items
              purchases?.forEach { purchase ->
                // Handle purchase.
              }
            }
            is PurchasesUpdatedResponse.Failure -> {
              when (purchasesUpdatedResponse.billingResponse) {
                BillingClient.BillingResponse.USER_CANCELED -> {
                  // Handle an error caused by a user cancelling the purchase flow.
                }
                else -> {
                  // Handle any other error codes.
                }
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.queryInAppSkuDetails(listOf("premium_upgrade", "gas"))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ skuDetailsResponse ->
          when (skuDetailsResponse) {
            is SkuDetailsResponse.Success -> {
              skuDetailsResponse.skuDetailsList.forEach { skuDetails ->
                // Handle result.
              }
            }
            is SkuDetailsResponse.Failure -> {
              when (skuDetailsResponse.billingResponse) {
              // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.querySubscriptionsSkuDetails(listOf("subscription_x", "subscription_y"))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ skuDetailsResponse ->
          when (skuDetailsResponse) {
            is SkuDetailsResponse.Success -> {
              skuDetailsResponse.skuDetailsList.forEach { skuDetails ->
                // Handle result.
              }
            }
            is SkuDetailsResponse.Failure -> {
              when (skuDetailsResponse.billingResponse) {
              // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.purchaseItem("premium_upgrade", activity)
        .subscribe({ purchaseResponse ->
          when (purchaseResponse) {
            PurchaseResponse.Success -> {
              // Billing flow launched successfully.
            }
            is PurchaseResponse.Failure -> {
              when (purchaseResponse.billingResponse) {
              // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.purchaseSubscription("subscription_x", activity)
        .subscribe({ purchaseResponse ->
          when (purchaseResponse) {
            PurchaseResponse.Success -> {
              // Billing flow launched successfully.
            }
            is PurchaseResponse.Failure -> {
              when (purchaseResponse.billingResponse) {
                // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.replaceSubscription("old_subscription_sku", "new_subscription_sku", activity)
        .subscribe({ purchaseResponse ->
          when (purchaseResponse) {
            PurchaseResponse.Success -> {
              // Billing flow launched successfully.
            }
            is PurchaseResponse.Failure -> {
              when (purchaseResponse.billingResponse) {
              // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.queryInAppPurchases()
        .subscribe({ queryPurchasesResponse ->
          when (queryPurchasesResponse) {
            is QueryPurchasesResponse.Success -> {
              queryPurchasesResponse.purchaseList.forEach { purchase ->
                // Handle purchase.
              }
            }
            is QueryPurchasesResponse.Failure -> {
              when (queryPurchasesResponse.billingResponse) {
                // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.querySubscriptionPurchases()
        .subscribe({ queryPurchasesResponse ->
          when (queryPurchasesResponse) {
            is QueryPurchasesResponse.Success -> {
              queryPurchasesResponse.purchaseList.forEach { purchase ->
                // Handle purchase.
              }
            }
            is QueryPurchasesResponse.Failure -> {
              when (queryPurchasesResponse.billingResponse) {
              // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.queryInAppPurchaseHistory()
        .subscribe({ queryPurchasesResponse ->
          when (queryPurchasesResponse) {
            is QueryPurchasesResponse.Success -> {
              queryPurchasesResponse.purchaseList.forEach { purchase ->
                // Handle purchase.
              }
            }
            is QueryPurchasesResponse.Failure -> {
              when (queryPurchasesResponse.billingResponse) {
              // Handle error codes.
              }
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.consumeItem("gas")
        .subscribe({ consumptionResponse ->
          when (consumptionResponse) {
            is ConsumptionResponse.Success -> {
              val outToken = consumptionResponse.outToken
              // Handle the success of the consumption operation.
            }
            is ConsumptionResponse.Failure -> {
              // Handle error codes.
            }
          }
        }, { e ->
          // Handle an unexpected error.
        })

    rxPlayBilling.connect()
        .flatMapSingle { connectionResult ->
          when (connectionResult) {
            ConnectionResult.Success -> rxPlayBilling.purchaseItem("my.sku.id", this)
            else -> throw Exception()
          }
        }
        .subscribe({ purchaseResponse: PurchaseResponse ->
          when (purchaseResponse) {
            PurchaseResponse.Success -> { } // Flow started successfully.
            is PurchaseResponse.Failure -> {
              // Something went wrong...
              val responseCode = purchaseResponse.billingResponse
              when (responseCode) {
                BillingClient.BillingResponse.BILLING_UNAVAILABLE -> {
                  // Handle error...
                }
                BillingClient.BillingResponse.DEVELOPER_ERROR -> {
                  // Handle error...
                }
                else -> {
                  // Handle other error codes...
                }
              }
            }
          }
        }, { e ->
          // Handle unexpected exception...
        })
  }

  override fun onDestroy() {
    rxPlayBilling.endConnection()
    super.onDestroy()
  }
}
