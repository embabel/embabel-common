/*
* Copyright 2025 Embabel Software, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.embabel.common.core

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Identifies a version we want to use: name and version.
 * Needs to be resolved at runtime.
 */
data class VersionSelection(
    @get:Schema(
        description = "name of the versioned entity",
        example = "holmes",
        required = true,
    )
    val name: String,
    @get:Schema(
        description = "desired version of the entity. If not specified, the latest version is used.",
        example = "6",
        required = false,
    )
    val version: Int? = null,
    // Only needed for persistence, can be ignored otherwise
    private val id: String? = null,
) {

    companion object {
        fun require(v: Versioned) = VersionSelection(v.name, v.version)
    }
}
