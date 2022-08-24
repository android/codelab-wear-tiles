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
import android.graphics.drawable.BitmapDrawable
import androidx.wear.tiles.RequestBuilders
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.wear.tiles.messaging.Contact
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Each contact in the tile state could have an avatar (represented by image url).
 *
 * If the image resources are requested (the ID is in [requestParams] or no IDs were specified),
 * then we fetch them from the network in this suspending function, returning the resulting bitmaps.
 */
internal suspend fun ImageLoader.fetchAvatarsFromNetwork(
    context: Context,
    requestParams: RequestBuilders.ResourcesRequest,
    tileState: MessagingTileState,
): Map<Contact, Bitmap> {
    val requestedAvatars: List<Contact> = if (requestParams.resourceIds.isEmpty()) {
        tileState.contacts
    } else {
        tileState.contacts.filter { contact ->
            requestParams.resourceIds.contains(contact.imageResourceId())
        }
    }

    val images = coroutineScope {
        requestedAvatars.map { contact ->
            async {
                val image = loadAvatar(context, contact)
                image?.let { contact to it }
            }
        }
    }.awaitAll().filterNotNull().toMap()

    return images
}

internal fun Contact.imageResourceId() = "contact:$id"

private suspend fun ImageLoader.loadAvatar(
    context: Context,
    contact: Contact,
    size: Int? = 64
): Bitmap? {
    val request = ImageRequest.Builder(context)
        .data(contact.avatarUrl)
        .apply {
            if (size != null) {
                size(size)
            }
        }
        .allowRgb565(true)
        .transformations(CircleCropTransformation())
        .allowHardware(false)
        .build()
    val response = execute(request)
    return (response.drawable as? BitmapDrawable)?.bitmap
}
