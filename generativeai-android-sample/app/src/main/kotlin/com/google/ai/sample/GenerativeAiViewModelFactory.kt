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

import android.content.ContentValues
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.sample.feature.chat.ChatViewModel
import com.google.ai.sample.feature.multimodal.PhotoReasoningViewModel
import com.google.ai.sample.feature.text.SummarizeViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
// Import Remote Config reqs
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
//import com.google.firebase.analytics


val GenerativeViewModelFactory = object : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        viewModelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val config = generationConfig {
            temperature = 0.7f
        }


        return with(viewModelClass) {
            remoteConfig = FirebaseRemoteConfig.getInstance() // Initialize here
            remoteConfig.fetchAndActivate()
                .addOnSuccessListener {
                    Log.d("MainActivity", "Remote Config values fetched and activated from GenerativeAIViewModelFactory")
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error fetching Remote Config from GenerativeAIViewModelFactory", e)
                }
            val apiKey = remoteConfig.getString("api_key")
                    Log.d("MainActivity", "got API key as $apiKey")
            when {
                isAssignableFrom(SummarizeViewModel::class.java) -> {
                    // *** Get model name from Remote Config ***
                    val modelName = remoteConfig.getString("summarize_model")
                            Log.d("got summarize model as ", remoteConfig.getString("summarize_model"))
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = apiKey,
                        generationConfig = config
                    )
                    SummarizeViewModel(generativeModel)
                }

                isAssignableFrom(PhotoReasoningViewModel::class.java) -> {
                    // *** Get model name from Remote Config ***
                    val modelName = remoteConfig.getString("photo_reasoning_model")
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = apiKey,
                        generationConfig = config
                    )
                    PhotoReasoningViewModel(generativeModel)
                }

                isAssignableFrom(ChatViewModel::class.java) -> {
                    // Get model name from Remote Config
                    val modelName = remoteConfig.getString("chat_reasoning_model")
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = apiKey,
                        generationConfig = config
                    )
                    ChatViewModel(generativeModel)
                }

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${viewModelClass.name}")
            }
        } as T
    }
}
