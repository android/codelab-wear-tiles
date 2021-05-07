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
import androidx.wear.tiles.builders.DimensionBuilders.*
import androidx.wear.tiles.builders.LayoutElementBuilders.*
import androidx.wear.tiles.builders.ModifiersBuilders.*
import androidx.wear.tiles.builders.ResourceBuilders.*
import androidx.wear.tiles.builders.TileBuilders.Tile
import androidx.wear.tiles.builders.TimelineBuilders.Timeline
import androidx.wear.tiles.builders.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.readers.DeviceParametersReaders.DeviceParameters
import androidx.wear.tiles.readers.RequestReaders.ResourcesRequest
import androidx.wear.tiles.readers.RequestReaders.TileRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

// TODO: Review Constants.
// Updating this version triggers a new call to onResourcesRequest(). This is useful for dynamic
// resources, the contents of which change even though their id stays the same (e.g. a graph).
// In this sample, our resources are all fixed, so we use a constant value.
private const val RESOURCES_VERSION = "1"

// dimensions
private val PROGRESS_BAR_THICKNESS = dp(6f)
private val BUTTON_SIZE = dp(48f)
private val BUTTON_RADIUS = dp(24f)
private val BUTTON_PADDING = dp(12f)
private val VERTICAL_SPACING_HEIGHT = dp(8f)

// Complete degrees for a circle (relates to [Arc] component)
private const val ARC_TOTAL_DEGREES = 360f

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

    // TODO: Build a Tile.
    override fun onTileRequest(requestParams: TileRequest) = serviceScope.future {

        // Retrieves progress value to populate the Tile.
        val goalProgress = GoalsRepository.getGoalProgress()
        // Retrieves device parameters to later retrieve font styles for any text in the Tile.
        val deviceParams = requestParams.deviceParameters

        // Creates Tile.
        Tile.builder()
            // If there are any graphics/images defined in the Tile's layout, the system will
            // retrieve them via onResourcesRequest() and match them with this version number.
            .setResourcesVersion(RESOURCES_VERSION)

            // Creates a timeline to hold one or more tile entries for a specific time periods.
            .setTimeline(
                Timeline.builder().addTimelineEntry(
                    TimelineEntry.builder().setLayout(
                        Layout.builder().setRoot(
                            // Creates the root [Box] [LayoutElement]
                            layout(goalProgress, deviceParams)
                        )
                    )
                )
            ).build()
    }

    // TODO: Supply resources (graphics) for the Tile.
    override fun onResourcesRequest(requestParams: ResourcesRequest) = serviceScope.future {
        Resources.builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                ID_IMAGE_START_RUN,
                ImageResource.builder()
                    .setAndroidResourceByResid(
                        AndroidImageResourceByResId.builder()
                            .setResourceId(R.drawable.ic_run)
                    )
            )
            .build()
    }

    // TODO: Review onDestroy() - cancellation of the serviceScope
    override fun onDestroy() {
        super.onDestroy()
        // Cleans up the coroutine
        serviceScope.cancel()
    }

    // TODO: Create root Box layout and content.
    // Creates a simple [Box] container that lays out its children one over the other. In our
    // case, an [Arc] that shows progress on top of a [Column] that includes the current steps
    // [Text], the total steps [Text], a [Spacer], and a running icon [Image].
    private fun layout(goalProgress: GoalProgress, deviceParameters: DeviceParameters) =
        Box.builder()
            // Sets width and height to expand and take up entire Tile space.
            .setWidth(expand())
            .setHeight(expand())

            // Adds an [Arc] via local function.
            .addContent(progressArc(goalProgress.percentage))

            // TODO: Add Column containing the rest of the data.
            // Adds a [Column] containing the two [Text] objects, a [Spacer], and a [Image].
            .addContent(
                Column.builder()
                    // Adds a [Text] via local function.
                    .addContent(
                        currentStepsText(goalProgress.current.toString(), deviceParameters)
                    )
                    // Adds a [Text] via local function.
                    .addContent(
                        totalStepsText(
                            resources.getString(R.string.goal, goalProgress.goal),
                            deviceParameters
                        )
                    )
                    // TODO: Add Spacer and Image representations of our step graphic.
                    // Adds a [Spacer].
                    .addContent(Spacer.builder().setHeight(VERTICAL_SPACING_HEIGHT))
                    // Adds an [Image] via local function.
                    .addContent(startRunButton())
            )
            .build()

    // TODO: Create a function that constructs an Arc representation of the current step progress.
    // Creates an [Arc] representing current progress towards steps goal.
    private fun progressArc(percentage: Float) = Arc.builder()
        .addContent(
            ArcLine.builder()
                // Uses degrees() helper to build an [AngularDimension] which represents progress.
                .setLength(degrees(percentage * ARC_TOTAL_DEGREES))
                .setColor(argb(ContextCompat.getColor(this, R.color.primary)))
                .setThickness(PROGRESS_BAR_THICKNESS)
        )
        // Element will start at 12 o'clock or 0 degree position in the circle.
        .setAnchorAngle(degrees(0.0f))
        // Aligns the contents of this container relative to anchor angle above.
        // ARC_ANCHOR_START - Anchors at the start of the elements. This will cause elements
        // added to an arc to begin at the given anchor_angle, and sweep around to the right.
        .setAnchorType(ARC_ANCHOR_START)
        .build()

    // TODO: Create functions that construct/stylize Text representations of the step count & goal.
    // Creates a [Text] with current step count and stylizes it.
    private fun currentStepsText(current: String, deviceParameters: DeviceParameters) = Text.builder()
        .setText(current)
        .setFontStyle(FontStyles.display2(deviceParameters))
        .build()

    // Creates a [Text] with total step count goal and stylizes it.
    private fun totalStepsText(goal: String, deviceParameters: DeviceParameters) = Text.builder()
        .setText(goal)
        .setFontStyle(FontStyles.title3(deviceParameters))
        .build()

    // TODO: Create a function that constructs/stylizes a clickable Image of a running icon.
    // Creates a running icon [Image] that's also a button to refresh the tile.
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
                    // TODO: Add click (START)
                    .setClickable(
                        Clickable.builder()
                            .setId(ID_CLICK_START_RUN)
                            .setOnClick(ActionBuilders.LoadAction.builder())
                    )
                    // TODO: Add click (END)
            )
            .build()
}
