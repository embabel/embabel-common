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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * A versioned object has a name and a version.
 * The combination should be unique, but there is also an id
 * Each version is immutable.
 */
@JsonDeserialize(`as` = SimpleVersioned::class)
@Schema(
    description = "A versioned object has a name and a version. The combination should be unique, but there is also an id. Each version is immutable.",
)
interface Versioned : Persistent {

    /**
     * A name should be stable.
     * New versions can be added with the same name.
     */
    @get:Schema(
        description = "Name of the object. Stable. Should be meaningful. New versions can be added with the same name.",
        example = "holmes",
        required = true,
    )
    val name: String

    /**
     * Our version of this, from 1
     */
    @get:Schema(
        description = "The present version of this object, from 1",
        example = "6",
        required = true,
    )
    val version: Int

    /**
     * Just the versioned information from this object.
     * Allows a Versioned object to be serialized without excessive size from its fields
     */
    fun versionInfo(): Versioned = SimpleVersioned(this)

}

/**
 * Used in versionInfo() method and as Spring Data Neo4j projection.
 */
data class SimpleVersioned(
    override val name: String,
    override val version: Int,
    override val id: String? = null,
) : Versioned {

    constructor(versioned: Versioned) : this(versioned.name, versioned.version, versioned.id)

    override fun toString(): String =
        "$name v$version" + (id?.let { " ($it)" } ?: "")
}

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Requested version not found")
class NoSuchVersionException(versionSelection: VersionSelection, note: String) :
    Exception("$note: No version ${versionSelection.version ?: "*"} of ${versionSelection.name} found")
