/*
 * Copyright 2025 The Android Open Source Project
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
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.CONTENT_SCALE_MODE_CROP
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FontSetting
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.material3.ButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.avatarImage
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textButton
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.clip
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.modifiers.toProtoLayoutModifiers
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices

fun MaterialScope.contactButton(contact: Contact): LayoutElement {
    val clickable = clickable() // TODO: Launch open conversation activity
    if (contact.avatarSource !is AvatarSource.None) {
        // If the contact has as associated image, display it.
        return avatarImage(
            protoLayoutResourceId = contact.imageResourceId(),
            width = expand(),
            height = expand(),
            modifier = LayoutModifier.clip(shapes.full).clickable(clickable),
            contentScaleMode = CONTENT_SCALE_MODE_CROP,
        )
    } else {
        // If the contact has no associated image, display a button with the contact's initials.

        // Simple function to return one of a set of themed button colors
        val colors = buttonColorsByIndex(contact.initials.hashCode())

        return textButton(
            onClick = clickable,
            labelContent = {
                text(
                    text = contact.initials.layoutString,
                    color = colors.labelColor,
                    settings = listOf(FontSetting.width(60F), FontSetting.weight(500)),
                )
            },
            width = expand(),
            height = expand(),
            contentPadding = padding(horizontal = 4F, vertical = 2F),
            colors = colors,
        )
    }
}

fun tileLayout(
    context: Context,
    deviceParameters: DeviceParameters,
    contacts: List<Contact>,
): LayoutElement {
    return materialScope(
        context = context,
        deviceConfiguration = deviceParameters,
        allowDynamicTheme = true,
    ) {
        TODO() // Add primaryLayout()
    }
}

/** Returns a set of [ButtonColors] based on the provided index [n]. */
private fun MaterialScope.buttonColorsByIndex(n: Int): ButtonColors =
    listOf(
        ButtonColors(
            labelColor = colorScheme.onPrimary,
            containerColor = colorScheme.primaryDim,
        ),
        ButtonColors(
            labelColor = colorScheme.onSecondary,
            containerColor = colorScheme.secondaryDim,
        ),
        ButtonColors(
            labelColor = colorScheme.onTertiary,
            containerColor = colorScheme.tertiaryDim,
        ),
    )
        .let { it[n.mod(it.size)] }

@Preview(device = WearDevices.SMALL_ROUND, name = "Small Round")
@Preview(device = WearDevices.LARGE_ROUND, name = "Large Round")
internal fun tileLayoutPreview(context: Context): TilePreviewData {
    val contacts = getMockContacts()
    return TilePreviewData(
        onTileRequest = { requestParams ->
            TilePreviewHelper.singleTimelineEntryTileBuilder(
                tileLayout(context, requestParams.deviceConfiguration, contacts)
            )
                .build()
        },
        // TODO: Add onTileResourceRequest
    )
}

fun Resources.Builder.addIdToImageMapping(id: String, @DrawableRes resId: Int): Resources.Builder =
    addIdToImageMapping(id, resId.toImageResource())
