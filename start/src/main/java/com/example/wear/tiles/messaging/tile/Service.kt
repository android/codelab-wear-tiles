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

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.ResourceBuilders.ImageResource
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import java.nio.ByteBuffer
import java.util.UUID

/**
 * Creates a Messaging tile, showing up to 6 contacts, an icon button and an edge button.
 *
 * It extends [SuspendingTileService], a Coroutine-friendly wrapper around
 * [androidx.wear.tiles.TileService], and implements [tileRequest] and [resourcesRequest].
 *
 * The main function, [tileRequest], is triggered when the system calls for a tile. Resources are
 * provided with the [resourcesRequest] method, which is triggered when the tile uses an Image.
 */
@OptIn(ExperimentalHorologistApi::class)
class MessagingTileService : SuspendingTileService() {
    private lateinit var imageLoader: ImageLoader
    private lateinit var contacts: List<Contact>

    override fun onCreate() {
        super.onCreate()

        // For this sample, contacts is a simple static list. In a real application it might come
        // from an injected repository.
        contacts = getMockContacts()

        // For this sample, make Coil dumb by disabling all caching features.
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context).memoryCache(null).diskCache(null).build()
        }

        imageLoader = SingletonImageLoader.get(this)
    }

    /** This method returns a Tile object, which describes the layout of the Tile. */
    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val layoutElement = tileLayout(this, requestParams.deviceConfiguration, contacts)

        // Resources are cached and keyed on resourcesVersion. If a Resources object with the same
        // resourcesVersion is present in the cache, resourcesRequest() is not called, and the
        // cached version is used instead. To ensure it is *always* called (e.g. for debugging), set
        // the version to a random string.
        val resourcesVersion =
            if (DEBUG_RESOURCES) {
                UUID.randomUUID().toString()
            } else {
                contacts.map { it.id }.toSortedSet().joinToString()
            }

        return Tile.Builder()
            .setResourcesVersion(resourcesVersion)
            .setTileTimeline(Timeline.fromLayoutElement(layoutElement))
            .build()
    }

    /**
     * This method returns a Tile Resources object that contains the image data required to render
     * and display the tile. It's passed a ResourcesRequest, one property of which is `resourceIds`
     * (a List<String>) which represents the resources we need to provide. (Note that this is just a
     * list of strings; these are *not* Android resource ids.)
     */
    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        TODO()
    }
}

const val DEBUG_RESOURCES = true

fun @receiver:DrawableRes Int.toImageResource(): ImageResource {
    return ImageResource.Builder()
        .setAndroidResourceByResId(
            ResourceBuilders.AndroidImageResourceByResId.Builder().setResourceId(this).build()
        )
        .build()
}

fun Bitmap.toImageResource(): ImageResource {
    val safeBitmap = this.copy(Bitmap.Config.RGB_565, false)

    val byteBuffer = ByteBuffer.allocate(safeBitmap.byteCount)
    safeBitmap.copyPixelsToBuffer(byteBuffer)
    val bytes: ByteArray = byteBuffer.array()

    return ImageResource.Builder()
        .setInlineResource(
            ResourceBuilders.InlineImageResource.Builder()
                .setData(bytes)
                .setWidthPx(this.width)
                .setHeightPx(this.height)
                .setFormat(ResourceBuilders.IMAGE_FORMAT_RGB_565)
                .build()
        )
        .build()
}
