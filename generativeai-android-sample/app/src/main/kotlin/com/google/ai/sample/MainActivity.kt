/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.sample

//import com.google.firebase.Firebase

//import com.google.firebase.analytics

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.ai.sample.feature.chat.ChatRoute
import com.google.ai.sample.feature.multimodal.PhotoReasoningRoute
import com.google.ai.sample.feature.text.SummarizeRoute
import com.google.ai.sample.ui.theme.GenerativeAISample
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.*
import java.util.*


private lateinit var firebaseAnalytics: FirebaseAnalytics

var remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

class MainActivity : ComponentActivity() {
    // Fake analytics in-app ad events

    // Fake in-app purchase event
    private fun getTransactionID(): String {
        return UUID.randomUUID().toString()
    }
    private fun onImpressionSuccess(adsValue: Double) {
        // The onImpressionSuccess will be reported when the rewarded video and interstitial ad is
        // opened.
        // For banners, the impression is reported on load success. Log.d(TAG, "onImpressionSuccess" +
        // impressionData)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, "SparkyInc")
        bundle.putString(FirebaseAnalytics.Param.AD_SOURCE, "External")
        bundle.putString(FirebaseAnalytics.Param.AD_FORMAT, "banner")
        bundle.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, "SparkyCoinBash")
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, adsValue)
        firebaseAnalytics.logEvent("ad_impression", bundle)
    }
    private fun onPurchaseSuccess(purchaseValue: Double) {
        Log.d("purchase", "purchase start")
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, getTransactionID())
        bundle.putString(FirebaseAnalytics.Param.AFFILIATION, "Google Store")
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, purchaseValue)
        bundle.putDouble(FirebaseAnalytics.Param.TAX, 2.58)
        bundle.putDouble(FirebaseAnalytics.Param.SHIPPING, 5.34)
        bundle.putString(FirebaseAnalytics.Param.COUPON, "WINTER_PROMO")
        //bundle.putString(FirebaseAnalytics.Param.ITEMS, "extra_coins")
        firebaseAnalytics.logEvent("purchase", bundle)
        /*
          firebaseAnalytics.logEvent(Event.PURCHASE) {
              param(FirebaseAnalytics.Param.TRANSACTION_ID, getTransactionID())
              param(FirebaseAnalytics.Param.AFFILIATION, "Google Store")
              param(FirebaseAnalytics.Param.CURRENCY, "USD")
              param(FirebaseAnalytics.Param.VALUE, purchaseValue)
              param(FirebaseAnalytics.Param.TAX, 2.58)
              param(FirebaseAnalytics.Param.SHIPPING, 5.34)
              param(FirebaseAnalytics.Param.COUPON, "WINTER_PROMO")
              param(FirebaseAnalytics.Param.ITEMS, "extra_coins")
          }
          */
        Log.d("purchase", "purchase end")
    }
    /*
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                param(FirebaseAnalytics.Param.AD_PLATFORM, "SparkyInc")
                param(FirebaseAnalytics.Param.AD_SOURCE, "External")
                param(FirebaseAnalytics.Param.AD_FORMAT, "banner")
                param(FirebaseAnalytics.Param.AD_UNIT_NAME, "SparkyCoinBash")
                param(FirebaseAnalytics.Param.CURRENCY, "USD")
                param(FirebaseAnalytics.Param.VALUE, adsValue)
            }
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get installation ID for testing
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Installations", "Installation ID: " + task.result)
            } else {
                Log.e("Installations", "Unable to get Installation ID")
            }
        }


        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                Log.d("MainActivity", "Remote Config values fetched and activated")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error fetching Remote Config", e)
            }
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate : ConfigUpdate) {
                Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys);

                if (configUpdate.updatedKeys.contains("welcome_message")) {
                    remoteConfig.activate().addOnCompleteListener {
                    }
                }
            }

            override fun onError(error : FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update error with code: " + error.code, error)
            }
        })


        // [START enable_dev_mode]
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // [END enable_dev_mode]

        // [START set_default_values]
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        // [END set_default_values]

        /* Start adding ad-clicks and purchase events */
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.setUserProperty("is_ai_user", "true")

        onImpressionSuccess(0.25)
            onPurchaseSuccess(1.50)
            onPurchaseSuccess(2.00)
            onImpressionSuccess(0.25)
            onPurchaseSuccess(0.10)
            onImpressionSuccess(1.50)


        /* End analytics */

        setContent {
            GenerativeAISample {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "menu") {
                        composable("menu") {
                            MenuScreen(onItemClicked = { routeId ->
                                navController.navigate(routeId)
                            })
                        }
                        composable("summarize") {
                            SummarizeRoute()
                        }
                        composable("photo_reasoning") {
                            PhotoReasoningRoute()
                        }
                        composable("chat") {
                            ChatRoute()
                        }
                    }
                }
            }
        }
    }
}
