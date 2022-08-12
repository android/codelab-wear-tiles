/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.wear.tiles.messaging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (intent.extras?.getString(EXTRA_JOURNEY)) {
                    EXTRA_JOURNEY_CONVERSATION -> {
                        val contact = intent.extras?.getString(EXTRA_CONVERSATION_CONTACT)!!
                        Text("Conversation: $contact")
                    }
                    EXTRA_JOURNEY_NEW -> {
                        Text("New conversation")
                    }
                    EXTRA_JOURNEY_SEARCH -> {
                        Text("Search for a conversation")
                    }
                    else -> Text("Opened from app launcher")
                }
            }
        }
    }

    companion object {
        internal const val EXTRA_JOURNEY = "journey"
        internal const val EXTRA_JOURNEY_CONVERSATION = "journey:conversation"
        internal const val EXTRA_JOURNEY_SEARCH = "journey:search"
        internal const val EXTRA_JOURNEY_NEW = "journey:new"
        internal const val EXTRA_CONVERSATION_CONTACT = "conversation:contact"
    }
}
