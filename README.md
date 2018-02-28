[![Build Status](https://travis-ci.org/jorgegil96/RxPlayBilling.svg?branch=master)](https://travis-ci.org/jorgegil96/RxPlayBilling)
[![Release status](https://img.shields.io/badge/release-v1.0.0-blue.svg)](https://github.com/jorgegil96/RxPlayBilling/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/jorgegil96/RxPlayBilling/issues)  


# RxPlayBilling for Android

RxPlayBilling is a simple, lightweight reactive wrapper around the new Android [Play Billing 
Library.](https://developer.android.com/google/play/billing/billing_library.html)

## Download
```
dependencies {
  implementation 'com.jorgegilcavazos.rxplaybilling:rxplaybilling:1.0.0'
}  
```

## Connecting to Google Play
Before you can use the RxPlayBilling client functions, you must first establish a connection
to Google Play:  
 ```
 rxPlayBilling = RxPlayBilling(context)
 rxPlayBilling.connect()
         .subscribe({ connectionResult ->
             when (connectionResult) {
                 ConnectionResult.Success -> {
                     // The billing client is ready. You can now query purchases.
                 }
                 is ConnectionResult.Failure -> {
                     // The billing client setup finished with response code
                     // $connectionResult.billingResponse
                 }
                 ConnectionResult.Disconnected -> {
                     // The billing client got disconnected, handle or attempt to reconnect.
                 }
             }
         }, { e ->
             // Handle unexpected error.
         })
 ```

## Retrieve existing purchases and subscriptions
```
rxPlayBilling.purchasesUpdates()
        .subscribe({ purchasesUpdatedResponse ->
            when (purchasesUpdatedResponse) {
                is PurchasesUpdatedResponse.Success -> {
                    val purchases = purchasesUpdatedResponse.items
                    purchases.forEach { purchase ->
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
```

## Making In-app Billing requests
Once the RxPlayBilling client connects to Google Play, you can initiate purchase requests for 
in-app products and subscriptions.

### Querying for items available for purchase
You can use RxPlayBilling to query item details for unique Product IDs from Google Play.

#### In-app purchases
```
rxPlayBilling.queryInAppSkuDetails(listOf("premium_upgrade", "gas"))
        .subscribe({ skuDetailsResponse ->
            when (skuDetailsResponse) {
                is SkuDetailsResponse.Success -> {
                    skuDetailsResponse.skuDetailsList.forEach { skuDetails ->
                        // Handle result.
                    }
                }
                is SkuDetailsResponse.Failure -> {
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
```


#### Subscriptions
```
rxPlayBilling.querySubscriptionsSkuDetails(listOf("subscription_x", "subscription_y"))
        .subscribe({ skuDetailsResponse ->
            when (skuDetailsResponse) {
                is SkuDetailsResponse.Success -> {
                    skuDetailsResponse.skuDetailsList.forEach { skuDetails ->
                        // Handle result.
                    }
                }
                is SkuDetailsResponse.Failure -> {
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
```
### Purchasing an item
To start a purchase request for an in-app item or subscription you can call the `purchaseItem()` 
or `purchaseSubscription` functions of the RxPlayBilling client, passing the sku id to start the 
billing flow for along with an activity reference.

#### In-app purchases
```
rxPlayBilling.purchaseItem("premium_upgrade", activity)
        .subscribe({ purchaseResponse ->
            when (purchaseResponse) {
                PurchaseResponse.Success -> {
                    // Billing flow launched successfully.
                }
                is PurchaseResponse.Failure -> {
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
```

#### Subscriptions
```
rxPlayBilling.purchaseSubscription("subscription_x", activity)
        .subscribe({ purchaseResponse ->
            when (purchaseResponse) {
                PurchaseResponse.Success -> {
                    // Billing flow launched successfully.
                }
                is PurchaseResponse.Failure -> {
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
```

If you wish to upgrade/downgrade a subscription, you must call the `replaceSubscription` 
function and pass the `oldSkuId` of the subscription that the user is 
upgrading/downgrading from, along with the `newSkuId` and an activity reference.

```
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
```

### Querying for purchased items
RxPlayBilling's `queryInAppPurchases()` and `querySubscriptionPurchases()` functions return the 
purchases made by the user account logged in to the device. The resulting list of purchases 
returns a cache of the Google Play Store app without initiating a network request. You can use 
the `queryInAppPurchaseHistory()` and `querySubscriptionPurchaseHistory()` functions to request 
the most recent purchases made by the user.

### In-app purchases
```
rxPayBilling.queryInAppPurchases()
        .subscribe({ queryPurchasesResponse ->
            when (queryPurchasesResponse) {
                is QueryPurchasesResponse.Success -> {
                    queryPurchasesResponse.purchaseList.forEach { purchase ->
                        // Handle purchase.
                    }
                }
                is QueryPurchasesResponse.Failure -> {
                    // Handle error codes.
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
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
```

#### Subscriptions
```
rxPlayBilling.querySubscriptionPurchases()
        .subscribe({ queryPurchasesResponse ->
            when (queryPurchasesResponse) {
                is QueryPurchasesResponse.Success -> {
                    queryPurchasesResponse.purchaseList.forEach { purchase ->
                        // Handle purchase.
                    }
                }
                is QueryPurchasesResponse.Failure -> {
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
        

rxPlayBilling.querySubscriptionPurchaseHistory()
        .subscribe({ queryPurchasesResponse ->
            when (queryPurchasesResponse) {
                is QueryPurchasesResponse.Success -> {
                    queryPurchasesResponse.purchaseList.forEach { purchase ->
                        // Handle purchase.
                    }
                }
                is QueryPurchasesResponse.Failure -> {
                    // Handle error codes.
                }
            }
        }, { e ->
            // Handle an unexpected error.
        })
```

### Consuming a purchase
You can consume an in-app product by calling the `consumeItem()` function and passing the 
`purchaseToken` of the item as a parameter.

```
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
```
