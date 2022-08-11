package com.example.wear.tiles.messaging

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.ByteBuffer

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
        tileState.contacts.filter {
            requestParams.resourceIds.contains(MessagingTileRenderer.ID_CONTACT_PREFIX + it.id)
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

/**
 * Utility to convert bitmaps (e.g. loaded from network or generated locally) to an ImageResource
 * that can be used in a tile.
 */
fun bitmapToImageResource(bitmap: Bitmap): ResourceBuilders.ImageResource {
    val safeBitmap = bitmap.toRgb565()

    val byteBuffer = ByteBuffer.allocate(safeBitmap.byteCount)
    safeBitmap.copyPixelsToBuffer(byteBuffer)
    val bytes: ByteArray = byteBuffer.array()

    return ResourceBuilders.ImageResource.Builder().setInlineResource(
        ResourceBuilders.InlineImageResource.Builder()
            .setData(bytes)
            .setWidthPx(bitmap.width)
            .setHeightPx(bitmap.height)
            .setFormat(ResourceBuilders.IMAGE_FORMAT_RGB_565)
            .build()
    )
        .build()
}

private fun Bitmap.toRgb565(): Bitmap {
    return this.copy(Bitmap.Config.RGB_565, false)
}
