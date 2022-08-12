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

import android.content.Context
import android.graphics.Bitmap
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.ResourceBuilders
import com.example.wear.tiles.R
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MainActivity
import com.example.wear.tiles.messaging.bitmapToImageResource
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement {
        return messagingTileLayout(
            context = context,
            deviceParameters = deviceParameters,
            state = state,
            contactClickableFactory = { contact ->
                launchActivityClickable(
                    clickableId = contact.id.toString(),
                    androidActivity = openConversation(contact)
                )
            },
            searchButtonClickable = launchActivityClickable("search_button", openSearch()),
            newButtonClickable = launchActivityClickable("new_button", openNewConversation())
        )
    }

    override fun ResourceBuilders.Resources.Builder.produceRequestedResources(
        resourceResults: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
        addIdToImageMapping(ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24))

        resourceResults.forEach { (contact, bitmap) ->
            addIdToImageMapping(
                /* id = */ "$ID_CONTACT_PREFIX${contact.id}",
                /* image = */ bitmapToImageResource(bitmap)
            )
        }
    }

    companion object {

        internal const val ID_IC_SEARCH = "ic_search"
        internal const val ID_CONTACT_PREFIX = "contact:"
    }
}

/**
 * Creates a Clickable that can be used to launch an activity.
 */
private fun launchActivityClickable(
    clickableId: String,
    androidActivity: ActionBuilders.AndroidActivity
) =
    ModifiersBuilders.Clickable.Builder()
        .setId(clickableId)
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(androidActivity)
                .build()
        )
        .build()

private fun openConversation(contact: Contact) = ActionBuilders.AndroidActivity.Builder()
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

private fun openSearch() = ActionBuilders.AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(
        MainActivity.EXTRA_JOURNEY,
        ActionBuilders.stringExtra(MainActivity.EXTRA_JOURNEY_SEARCH)
    )
    .build()

private fun openNewConversation() = ActionBuilders.AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(
        MainActivity.EXTRA_JOURNEY,
        ActionBuilders.stringExtra(MainActivity.EXTRA_JOURNEY_NEW)
    )
    .build()

private fun ActionBuilders.AndroidActivity.Builder.setMessagingActivity(): ActionBuilders.AndroidActivity.Builder {
    return setPackageName("com.example.wear.tiles")
        .setClassName("com.example.wear.tiles.messaging.MainActivity")
}
