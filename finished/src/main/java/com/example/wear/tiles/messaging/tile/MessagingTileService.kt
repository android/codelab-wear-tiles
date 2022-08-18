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
package com.example.wear.tiles.messaging.tile

import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import coil.imageLoader
import com.example.wear.tiles.messaging.MessagingRepo
import com.google.android.horologist.tiles.CoroutinesTileService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MessagingTileService : CoroutinesTileService() {

    private lateinit var repo: MessagingRepo
    private lateinit var renderer: MessagingTileRenderer
    private lateinit var tileStateFlow: StateFlow<MessagingTileState?>

    override fun onCreate() {
        super.onCreate()
        repo = MessagingRepo(this)
        renderer = MessagingTileRenderer(this)
        tileStateFlow = repo.getFavoriteContacts()
            .map { contacts -> MessagingTileState(contacts) }
            .stateIn(
                lifecycleScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    override suspend fun tileRequest(requestParams: TileRequest): Tile {
        val tileState = latestTileState()
        return renderer.renderTimeline(tileState, requestParams)
    }

    /**
     * Reads the latest state from the flow, and updates the data if there isn't any.
     */
    private suspend fun latestTileState(): MessagingTileState {
        var tileState = tileStateFlow.filterNotNull().first()

        // see `refreshData()` docs for more information
        if (tileState.contacts.isEmpty()) {
            refreshData()
            tileState = tileStateFlow.filterNotNull().first()
        }
        return tileState
    }

    /**
     * If our data source (the repository) is empty/has stale data, this is where we could perform
     * an update. For this sample, we're updating the repository with fake data
     * ([MessagingRepo.knownContacts]).
     *
     * In a more complete example, tiles, complications and the main app (/overlay) would
     * share a common data source so it's less likely that an initial data refresh triggered by the
     * tile would be necessary.
     */
    private suspend fun refreshData() {
        repo.updateContacts(MessagingRepo.knownContacts)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        // Since we know there's only 2 very small avatars, we'll fetch them
        // as part of this resource request.
        val avatars = imageLoader.fetchAvatarsFromNetwork(
            context = this@MessagingTileService,
            requestParams = requestParams,
            tileState = latestTileState()
        )
        // then pass the bitmaps to the renderer to transform them to ImageResources
        return renderer.produceRequestedResources(avatars, requestParams)
    }
}
