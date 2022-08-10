package com.example.wear.tiles

import android.content.Context
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ResourceBuilders
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<GoalProgress, Unit>(context) {

    override fun renderTile(
        state: GoalProgress, // TODO: message state
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement {
        return layout(
            context,
            state,
            deviceParameters
        )
    }

    override fun ResourceBuilders.Resources.Builder.produceRequestedResources(
        resourceResults: Unit,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
        addIdToImageMapping(
            ID_IMAGE_START_RUN,
            drawableResToImageResource(R.drawable.ic_message_24)
        )

        // TODO: other avatars
    }
}
