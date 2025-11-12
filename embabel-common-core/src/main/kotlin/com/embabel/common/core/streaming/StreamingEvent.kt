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
package com.embabel.common.core.streaming

/**
 * Sealed class representing events in a streaming operation.
 * Provides type-safe handling of both object results and thinking content.
 * Supports Either-like functional programming patterns.
 */
sealed class StreamingEvent<out T> {

    /**
     * Event representing a parsed object from the stream.
     * @param item The parsed object instance
     */
    data class Object<T>(val item: T) : StreamingEvent<T>()

    /**
     * Event representing thinking content from the process.
     * @param content The thinking text content
     */
    data class Thinking(val content: String) : StreamingEvent<Nothing>()

    /**
     * Either-style fold operation for functional composition.
     * @param left Function to handle thinking content
     * @param right Function to handle object content
     */
    inline fun <R> fold(
        left: (String) -> R,
        right: (T) -> R
    ): R = when (this) {
        is Thinking -> left(content)
        is Object -> right(item)
    }

    /**
     * Check if this event is an object
     */
    fun isObject(): Boolean = this is Object

    /**
     * Check if this event is thinking
     */
    fun isThinking(): Boolean = this is Thinking
}
