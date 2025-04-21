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
package com.embabel.common.core

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Interface for objects that can have a unique id,
 * but can also be in a transient state.
 * The object may or may not be persisted
 */
interface Identified {

    /**
     * The id of the object will be set once persisted
     */
    @get:Schema(
        description = "persistent id",
        example = "aa0fd6de-2318-41e3-9dd4-104c6e02392f",
        required = false,
    )
    val id: String?
        get() = null

    /**
     * Is this object persistent? Not the same as whether it has been persisted
     */
    fun persistent(): Boolean

    companion object {

        operator fun invoke(id: String): Identified {
            return object : Identified {
                override val id = id
                override fun persistent(): Boolean = true
            }
        }
    }

}

/**
 * Identified object that is persistent
 */
interface Persistent : Identified {

    override fun persistent(): Boolean = true

}

interface Ephemeral : Identified {

    override fun persistent(): Boolean = false

}
