package com.example.wear.tiles

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders

private val PROGRESS_BAR_THICKNESS = DimensionBuilders.dp(6f)
private val BUTTON_SIZE = DimensionBuilders.dp(48f)
private val BUTTON_RADIUS = DimensionBuilders.dp(24f)
private val BUTTON_PADDING = DimensionBuilders.dp(12f)
private val VERTICAL_SPACING_HEIGHT = DimensionBuilders.dp(8f)
private const val ARC_TOTAL_DEGREES = 360f
internal const val ID_IMAGE_START_RUN = "image_start_run"
private const val ID_CLICK_START_RUN = "click_start_run"

internal fun layout(
    context: Context,
    goalProgress: GoalProgress,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = LayoutElementBuilders.Box.Builder()
    // Sets width and height to expand and take up entire Tile space.
    .setWidth(DimensionBuilders.expand())
    .setHeight(DimensionBuilders.expand())

    // Adds an [Arc] via local function.
    .addContent(progressArc(context, goalProgress.percentage))

    // TODO: Add Column containing the rest of the data.
    // Adds a [Column] containing the two [Text] objects, a [Spacer], and a [Image].
    .addContent(
        LayoutElementBuilders.Column.Builder()
            // Adds a [Text] via local function.
            .addContent(
                currentStepsText(goalProgress.current.toString(), deviceParameters)
            )
            // Adds a [Text] via local function.
            .addContent(
                totalStepsText(
                    context.getString(R.string.goal, goalProgress.goal),
                    deviceParameters
                )
            )
            // TODO: Add Spacer and Image representations of our step graphic.
            // Adds a [Spacer].
            .addContent(
                LayoutElementBuilders.Spacer.Builder().setHeight(VERTICAL_SPACING_HEIGHT).build()
            )
            // Adds an [Image] via local function.
            .addContent(startRunButton(context))
            .build()
    )
    .build()

// TODO: Create a function that constructs an Arc representation of the current step progress.
// Creates an [Arc] representing current progress towards steps goal.
private fun progressArc(context: Context, percentage: Float) = LayoutElementBuilders.Arc.Builder()
    .addContent(
        LayoutElementBuilders.ArcLine.Builder()
            // Uses degrees() helper to build an [AngularDimension] which represents progress.
            .setLength(DimensionBuilders.degrees(percentage * ARC_TOTAL_DEGREES))
            .setColor(ColorBuilders.argb(ContextCompat.getColor(context, R.color.primary)))
            .setThickness(PROGRESS_BAR_THICKNESS)
            .build()
    )
    // Element will start at 12 o'clock or 0 degree position in the circle.
    .setAnchorAngle(DimensionBuilders.degrees(0.0f))
    // Aligns the contents of this container relative to anchor angle above.
    // ARC_ANCHOR_START - Anchors at the start of the elements. This will cause elements
    // added to an arc to begin at the given anchor_angle, and sweep around to the right.
    .setAnchorType(LayoutElementBuilders.ARC_ANCHOR_START)
    .build()

// TODO: Create functions that construct/stylize Text representations of the step count & goal.
// Creates a [Text] with current step count and stylizes it.
private fun currentStepsText(
    current: String,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = LayoutElementBuilders.Text.Builder()
    .setText(current)
    .setFontStyle(LayoutElementBuilders.FontStyles.display2(deviceParameters).build())
    .build()

// Creates a [Text] with total step count goal and stylizes it.
private fun totalStepsText(
    goal: String,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = LayoutElementBuilders.Text.Builder()
    .setText(goal)
    .setFontStyle(LayoutElementBuilders.FontStyles.title3(deviceParameters).build())
    .build()

// Creates a running icon [Image] that's also a button to refresh the tile.
private fun startRunButton(context: Context) =
    LayoutElementBuilders.Image.Builder()
        .setWidth(BUTTON_SIZE)
        .setHeight(BUTTON_SIZE)
        .setResourceId(ID_IMAGE_START_RUN)
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setPadding(
                    ModifiersBuilders.Padding.Builder()
                        .setStart(BUTTON_PADDING)
                        .setEnd(BUTTON_PADDING)
                        .setTop(BUTTON_PADDING)
                        .setBottom(BUTTON_PADDING)
                        .build()
                )
                .setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setCorner(
                            ModifiersBuilders.Corner.Builder().setRadius(BUTTON_RADIUS).build()
                        )
                        .setColor(
                            ColorBuilders.argb(
                                ContextCompat.getColor(
                                    context,
                                    R.color.primaryDark
                                )
                            )
                        )
                        .build()
                )
                // TODO: Add click (START)
                .setClickable(
                    ModifiersBuilders.Clickable.Builder()
                        .setId(ID_CLICK_START_RUN)
                        .setOnClick(ActionBuilders.LoadAction.Builder().build())
                        .build()
                )
                // TODO: Add click (END)
                .build()
        )
        .build()