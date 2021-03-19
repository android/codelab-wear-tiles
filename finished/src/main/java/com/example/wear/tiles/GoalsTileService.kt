/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.wear.tiles

import androidx.core.content.ContextCompat
import androidx.wear.tiles.TileProviderService
import androidx.wear.tiles.builders.*
import androidx.wear.tiles.builders.ColorBuilders.argb
import androidx.wear.tiles.builders.DimensionBuilders.degrees
import androidx.wear.tiles.builders.DimensionBuilders.dp
import androidx.wear.tiles.builders.DimensionBuilders.sp
import androidx.wear.tiles.builders.LayoutElementBuilders.*
import androidx.wear.tiles.builders.ModifiersBuilders.*
import androidx.wear.tiles.builders.ResourceBuilders.*
import androidx.wear.tiles.builders.TileBuilders.Tile
import androidx.wear.tiles.builders.TimelineBuilders.Timeline
import androidx.wear.tiles.builders.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.readers.RequestReaders.ResourcesRequest
import androidx.wear.tiles.readers.RequestReaders.TileRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

// Updating this version triggers a new call to onResourcesRequest(). This is useful for dynamic
// resources, the contents of which change even though their id stays the same (e.g. a graph).
// In this sample, our resources are all fixed, so we use a constant value.
private const val RESOURCES_VERSION = "1"

// dimensions
private val PROGRESS_BAR_THICKNESS = dp(6f)
private val BUTTON_SIZE = dp(48f)
private val BUTTON_RADIUS = dp(24f)
private val BUTTON_PADDING = dp(12f)
private val VERTICAL_SPACING_BUTTON = dp(8f)

// identifiers
private const val ID_IMAGE_START_RUN = "image_start_run"
private const val ID_CLICK_START_RUN = "click_start_run"

/**
 * Creates a Fitness Tile, showing your progress towards a daily goal. The progress is defined
 * randomly, for demo purposes only. A new random progress is shown when the user taps the button.
 */
class GoalsTileService : TileProviderService() {
    // For coroutines, use a custom scope we can cancel when the service is destroyed
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onTileRequest(
        requestParams: TileRequest
    ) = serviceScope.future {
        val goalProgress = GoalsRepository.getGoalProgress()
        val fontStyles = FontStyles.withDeviceParameters(requestParams.deviceParameters)
        Tile.builder()
            .setResourcesVersion(RESOURCES_VERSION)
            // Creates a timeline to hold one or more tile entries for a specific time periods.
            .setTimeline(
                Timeline.builder().addTimelineEntry(
                    TimelineEntry.builder().setLayout(
                        Layout.builder().setRoot(
                            layout(goalProgress, fontStyles)
                        )
                    )
                )
            ).build()
    }

    override fun onResourcesRequest(requestParams: ResourcesRequest) = serviceScope.future {
        Resources.builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                ID_IMAGE_START_RUN,
                ImageResource.builder().setAndroidResourceByResid(
                    AndroidImageResourceByResId.builder()
                        .setResourceId(R.drawable.ic_run)
                        .build()
                )
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleans up the coroutine
        serviceScope.cancel()
    }

    private fun layout(goalProgress: GoalProgress, fontStyles: FontStyles) =
        Box.builder()
            .addContent(progressArc(goalProgress.percentage))
            .addContent(
                Column.builder()
                    .addContent(currentStepsText(goalProgress.current.toString(), fontStyles))
                    .addContent(
                        totalStepsText(
                            resources.getString(R.string.goal, goalProgress.goal),
                            fontStyles
                        )
                    )
                    .addContent(Spacer.builder().setHeight(VERTICAL_SPACING_BUTTON))
                    .addContent(startRunButton())
            )

    private fun progressArc(percentage: Float) =
        Arc.builder()
            .addContent(
                ArcLine.builder()
                    .setLength(degrees(percentage * 360f))
                    .setColor(
                        argb(ContextCompat.getColor(this, R.color.primary))
                    )
                    .setThickness(PROGRESS_BAR_THICKNESS)
            )
            .setAnchorType(ARC_ANCHOR_START)

    private fun currentStepsText(current: String, fontStyles: FontStyles) =
        Text.builder()
            .setText(current)
            .setFontStyle(FontStyle.builder().setSize(sp(44f)).build())
            .setFontStyle(fontStyles.display2())

    private fun totalStepsText(goal: String, fontStyles: FontStyles) = Text.builder()
        .setText(goal)
        .setFontStyle(FontStyle.builder().setSize(sp(44f)).build())
        .setFontStyle(fontStyles.title3())

    // Layout for an icon button that refreshes the screen
    private fun startRunButton() =
        Image.builder()
            .setWidth(BUTTON_SIZE)
            .setHeight(BUTTON_SIZE)
            .setResourceId(ID_IMAGE_START_RUN)
            .setModifiers(
                Modifiers.builder()
                    .setPadding(
                        Padding.builder()
                            .setStart(BUTTON_PADDING)
                            .setEnd(BUTTON_PADDING)
                            .setTop(BUTTON_PADDING)
                            .setBottom(BUTTON_PADDING)
                    )
                    .setBackground(
                        Background.builder()
                            .setCorner(Corner.builder().setRadius(BUTTON_RADIUS))
                            .setColor(argb(ContextCompat.getColor(this, R.color.primaryDark)))
                    )
                    .setClickable(
                        Clickable.builder()
                            .setId(ID_CLICK_START_RUN)
                            .setOnClick(ActionBuilders.LoadAction.builder())
                    )
            )
}
