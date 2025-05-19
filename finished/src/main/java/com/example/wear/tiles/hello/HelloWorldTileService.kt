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
package com.example.wear.tiles.hello

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.tiles.R
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService

private const val RESOURCES_VERSION = "0"

@ExperimentalHorologistApi
class HelloWorldTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): Tile {
        return Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                Timeline.fromLayoutElement(
                    tileLayout(
                        this,
                        requestParams.deviceConfiguration,
                        getString(R.string.hello_tile_body),
                    )
                )
            )
            .build()
    }
}

fun tileLayout(
    context: Context,
    deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    message: String,
) =
    materialScope(
        context = context,
        deviceConfiguration = deviceConfiguration,
        allowDynamicTheme = false,
    ) {
        primaryLayout(mainSlot = { text(message.layoutString) })
    }

@Preview(device = WearDevices.SMALL_ROUND, name = "Small Round")
@Preview(device = WearDevices.LARGE_ROUND, name = "Large Round")
internal fun helloLayoutPreview(context: Context): TilePreviewData {
    return TilePreviewData(
        onTileRequest = { requestParams ->
            TilePreviewHelper.singleTimelineEntryTileBuilder(
                    tileLayout(context, requestParams.deviceConfiguration, "Hello, preview tile!")
                )
                .build()
        }
    )
}
