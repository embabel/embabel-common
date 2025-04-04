/*
* Copyright 2025 Embabel Software, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.embabel.common.util.kotlin

import java.time.Instant
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

/**
 * Convert an object to a map.
 */
inline fun <reified T : Any> T.toMap(
    excludePropertyFilter: (KProperty<*>) -> Boolean = { false },
    excludeKeyFilter: (String) -> Boolean = { false },
): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return this::class.memberProperties
        .filterNot { excludePropertyFilter(it) }
        .associateWith { it.getter.call(this) }
        .filter { it.value != null }
        .filterNot { excludeKeyFilter(it.key.name) }
        .mapKeys { it.key.name } as Map<String, Any>
}

inline fun <reified T : Any> T.toMap(): Map<String, Any> = toMap({ false }, { false })


fun propertyToInstant(map: Map<String, Any?>, name: String): Instant? =
    (map[name] as? Long)?.let {
        Instant.ofEpochMilli(
            it
        )
    }
