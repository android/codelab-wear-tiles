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
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ActionBuilders.AndroidActivity
import androidx.wear.tiles.ActionBuilders.stringExtra
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.layouts.MultiButtonLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.example.wear.tiles.R
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MainActivity.Companion.EXTRA_CONVERSATION_CONTACT
import com.example.wear.tiles.messaging.MainActivity.Companion.EXTRA_JOURNEY
import com.example.wear.tiles.messaging.MainActivity.Companion.EXTRA_JOURNEY_CONVERSATION
import com.example.wear.tiles.messaging.MainActivity.Companion.EXTRA_JOURNEY_NEW
import com.example.wear.tiles.messaging.MainActivity.Companion.EXTRA_JOURNEY_SEARCH
import com.example.wear.tiles.messaging.MessagingRepo
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.compose.tools.LayoutElementPreview
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.WearSmallRoundDevicePreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.images.drawableResToImageResource

/**
 * Layout definition for the Messaging Tile.
 *
 * By separating the layout completely, we can pass fake data for the [MessageTilePreview] so it can
 * be rendered in Android Studio (use the "Split" or "Design" editor modes).
 */
internal fun messagingTileLayout(
    state: MessagingTileState,
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = PrimaryLayout.Builder(deviceParameters)
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
                            clickable = launchActivityClickable(
                                clickableId = "${contact.id}",
                                androidActivity = openConversation(contact)
                            )
                        )
                    )
                }
            }
            .addButtonContent(
                searchLayout(
                    context = context,
                    clickable = launchActivityClickable(
                        clickableId = "search_button",
                        androidActivity = openSearch()
                    )
                )
            )
            .build()
    ).setPrimaryChipContent(
        CompactChip.Builder(
            /* context = */ context,
            /* text = */ context.getString(R.string.tile_messaging_create_new),
            /* clickable = */ launchActivityClickable(
                clickableId = "new_conversation_button",
                androidActivity = openNewConversation()
            ),
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

private fun Contact.imageResourceId() = "${MessagingTileRenderer.ID_CONTACT_PREFIX}$id"

private fun searchLayout(
    context: Context,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(context.getString(R.string.tile_messaging_search))
    .setIconContent(MessagingTileRenderer.ID_IC_SEARCH)
    .setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
    .build()

private fun launchActivityClickable(clickableId: String, androidActivity: AndroidActivity) =
    ModifiersBuilders.Clickable.Builder()
        .setId(clickableId)
        .setOnClick(
            ActionBuilders.LaunchAction.Builder()
                .setAndroidActivity(androidActivity)
                .build()
        )
        .build()

private fun openConversation(contact: Contact) = AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(EXTRA_JOURNEY, stringExtra(EXTRA_JOURNEY_CONVERSATION))
    .addKeyToExtraMapping(EXTRA_CONVERSATION_CONTACT, stringExtra(contact.name))
    .build()

private fun openSearch() = AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(EXTRA_JOURNEY, stringExtra(EXTRA_JOURNEY_SEARCH))
    .build()

private fun openNewConversation() = AndroidActivity.Builder()
    .setMessagingActivity()
    .addKeyToExtraMapping(EXTRA_JOURNEY, stringExtra(EXTRA_JOURNEY_NEW))
    .build()

private fun AndroidActivity.Builder.setMessagingActivity(): AndroidActivity.Builder {
    return setPackageName("com.example.wear.tiles")
        .setClassName("com.example.wear.tiles.messaging.MainActivity")
}

@WearSmallRoundDevicePreview
@Composable
private fun MessageTilePreview() {
    val context = LocalContext.current
    val state = MessagingTileState(MessagingRepo.knownContacts)
    LayoutRootPreview(
        messagingTileLayout(
            state,
            context,
            buildDeviceParameters(context.resources)
        )
    ) {
        addIdToImageMapping(
            state.contacts[1].imageResourceId(),
            drawableResToImageResource(R.drawable.ali)
        )
        addIdToImageMapping(
            state.contacts[2].imageResourceId(),
            drawableResToImageResource(R.drawable.taylor)
        )
        addIdToImageMapping(
            MessagingTileRenderer.ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24)
        )
    }
}

@IconSizePreview
@Composable
private fun SearchButtonPreview() {
    LayoutElementPreview(
        searchLayout(
            context = LocalContext.current,
            clickable = emptyClickable
        )
    ) {
        addIdToImageMapping(
            MessagingTileRenderer.ID_IC_SEARCH,
            drawableResToImageResource(R.drawable.ic_search_24)
        )
    }
}

@Preview(
    backgroundColor = 0xff000000,
    showBackground = true,
    widthDp = 100,
    heightDp = 100
)
public annotation class IconSizePreview
