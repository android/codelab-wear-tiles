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
package com.example.wear.tiles.messaging.tile

import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ModifiersBuilders
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MainActivity

/**
 * Creates a Clickable that can be used to launch an activity.
 */
internal fun launchActivityClickable(
    clickableId: String,
    androidActivity: ActionBuilders.AndroidActivity
) = ModifiersBuilders.Clickable.Builder()
    .setId(clickableId)
    .setOnClick(
        ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(androidActivity)
            .build()
    )
    .build()

internal fun openConversation(contact: Contact) = ActionBuilders.AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(
        MainActivity.EXTRA_JOURNEY,
        ActionBuilders.stringExtra(MainActivity.EXTRA_JOURNEY_CONVERSATION)
    )
    .addKeyToExtraMapping(
        MainActivity.EXTRA_CONVERSATION_CONTACT,
        ActionBuilders.stringExtra(contact.name)
    )
    .build()

internal fun openSearch() = ActionBuilders.AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(
        MainActivity.EXTRA_JOURNEY,
        ActionBuilders.stringExtra(MainActivity.EXTRA_JOURNEY_SEARCH)
    )
    .build()

internal fun openNewConversation() = ActionBuilders.AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(
        MainActivity.EXTRA_JOURNEY,
        ActionBuilders.stringExtra(MainActivity.EXTRA_JOURNEY_NEW)
    )
    .build()

internal fun ActionBuilders.AndroidActivity.Builder.setMessagingActivity(): ActionBuilders.AndroidActivity.Builder {
    return setPackageName("com.example.wear.tiles")
        .setClassName("com.example.wear.tiles.messaging.MainActivity")
}
