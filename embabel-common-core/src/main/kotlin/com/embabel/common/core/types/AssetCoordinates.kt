/*
 * Copyright 2024-2025 Embabel Software, Inc.
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
package com.embabel.common.core.types

import io.swagger.v3.oas.annotations.media.Schema

@JvmInline
value class Semver(val value: String = DEFAULT_VERSION) {
    init {
        require(value.isNotBlank()) { "Semver must not be blank" }
        require(value.matches(Regex("^[0-9]+\\.[0-9]+\\.[0-9]+(-[a-zA-Z0-9]+)?\$"))) {
            "Semver must be in the format X.Y.Z or X.Y.Z-alpha"
        }
    }

    constructor(major: Int = 0, minor: Int = 1, patch: Int = 0) : this(
        "$major.$minor.$patch",
    )

    override fun toString(): String = value

    companion object {
        /**
         * Default version for anything versioned
         */
        const val DEFAULT_VERSION = "0.1.0-SNAPSHOT"
    }
}

/**
 * A versioned asset has a name and a version.
 * The combination should be unique, but there is also an id
 * Each version is immutable.
 */
@Schema(
    description = "Like Maven coordinates, for assets such as agents",
)
interface AssetCoordinates : Named {

    /**
     * Provider of the object.
     */
    @get:Schema(
        description = "Provider of the asset. New versions can be added with the same name.",
        example = "Embabel",
        required = true,
    )
    val provider: String

    /**
     * Name of the asset.
     * New versions can be added with the same name.
     * The combination of provider and name should be unique.
     */
    @get:Schema(
        description = "Name of the object. Stable. Should be meaningful. New versions can be added with the same name.",
        example = "CodingAgent",
        required = true,
    )
    override val name: String

    @get:Schema(
        description = "The version of the asset. Semver format.",
        example = "0.1.2",
        required = true,
    )
    val version: Semver

}
