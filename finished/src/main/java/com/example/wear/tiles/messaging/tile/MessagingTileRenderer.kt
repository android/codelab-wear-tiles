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
@file:OptIn(ExperimentalHorologistApi::class)

package com.example.wear.tiles.messaging.tile

import android.content.Context
import android.graphics.Bitmap
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.tiles.R
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MessagingRepo.Companion.knownContacts
import com.example.wear.tiles.messaging.tile.MessagingTileRenderer.Companion.ID_IC_SEARCH
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.images.toImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): androidx.wear.protolayout.LayoutElementBuilders.LayoutElement {
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

    override fun Resources.Builder.produceRequestedResources(
        resourceState: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: List<String>
    ) {
        addIdToImageMapping(ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24))

        resourceState.forEach { (contact, bitmap) ->
            addIdToImageMapping(
                /* id = */ contact.imageResourceId(),
                /* image = */ bitmap.toImageResource()
            )
        }
    }

    companion object {
        internal const val ID_IC_SEARCH = "ic_search"
    }

}

/**
 * Layout definition for the Messaging Tile.
 *
 * By separating the layout completely, we can pass fake data for the [MessageTilePreview] so it can
 * be rendered in Android Studio (use the "Split" or "Design" editor modes).
 */
private fun messagingTileLayout(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    state: MessagingTileState,
    contactClickableFactory: (Contact) -> ModifiersBuilders.Clickable,
    searchButtonClickable: ModifiersBuilders.Clickable,
    newButtonClickable: ModifiersBuilders.Clickable
) = PrimaryLayout.Builder(deviceParameters)
    .setResponsiveContentInsetEnabled(true)
    .setContent(
        MultiButtonLayout.Builder()
            .apply {
                // In a PrimaryLayout with a compact chip at the bottom, we can fit 5 buttons.
                // We're only taking the first 4 contacts so that we can fit a Search button too.
                state.contacts.take(4).forEach { contact ->
                    addButtonContent(
                        contactLayout(
                            context = context,
                            contact = contact,
                            clickable = contactClickableFactory(contact)
                        )
                    )
                }
            }
            .addButtonContent(searchLayout(context, searchButtonClickable))
            .build()
    )
    .setPrimaryChipContent(
        CompactChip.Builder(
            /* context = */ context,
            /* text = */ context.getString(R.string.tile_messaging_create_new),
            /* clickable = */ newButtonClickable,
            /* deviceParameters = */ deviceParameters
        )
            .setChipColors(ChipColors.primaryChipColors(MessagingTileTheme.colors))
            .build()
    )
    .build()

private fun contactLayout(
    context: Context,
    contact: Contact,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(contact.name)
    .apply {
        if (contact.avatarUrl != null) {
            setImageContent(contact.imageResourceId())
        } else {
            setTextContent(contact.initials)
            setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
        }
    }
    .build()

private fun searchLayout(
    context: Context,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(context.getString(R.string.tile_messaging_search))
    .setIconContent(ID_IC_SEARCH)
    .setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
    .build()

private fun previewResources() = Resources.Builder()
    .addIdToImageMapping(ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24))
    .addIdToImageMapping(
        knownContacts[1].imageResourceId(),
        drawableResToImageResource(R.drawable.ali)
    )
    .addIdToImageMapping(
        knownContacts[2].imageResourceId(),
        drawableResToImageResource(R.drawable.taylor)
    )
    .build()

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun messagingTileLayoutPreview(context: Context): TilePreviewData {

    return TilePreviewData({ previewResources() }) { request ->
        MessagingTileRenderer(context).renderTimeline(
            MessagingTileState(knownContacts),
            request
        )
    }
}
