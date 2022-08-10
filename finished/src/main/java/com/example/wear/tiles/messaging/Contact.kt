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
package com.example.wear.tiles.messaging

data class Contact(
    val id: Long,
    val initials: String,
    val name: String,
    val avatarUrl: String?
) {
    fun toPreferenceString(): String =
        listOf(id, initials, name, avatarUrl.orEmpty()).joinToString(",")

    companion object {
        fun String.toContact(): Contact {
            val (id, initials, name, avatarUrl) = split(",")

            return Contact(id.toLong(), initials, name, avatarUrl.ifBlank { null })
        }
    }
}
