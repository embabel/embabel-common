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
 * Thinking state classifications for multi-line thinking block processing.
 * Enables reactive streaming of thinking content with proper state tracking.
 */
enum class ThinkingState {
    /** Default state - no specific state classification attempted */
    NONE,
    /** Complete thinking block on single line: <think>content</think> */
    BOTH,
    /** Start of thinking block: <think> (content may continue on next lines) */
    START,
    /** End of thinking block: </think> (completes multi-line thinking) */
    END,
    /** Continuation of thinking content (no start/end markers) */
    CONTINUATION
}

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
     * @param state The thinking state indicating position within multi-line blocks
     */
    data class Thinking(
        val content: String,
        val state: ThinkingState = ThinkingState.NONE
    ) : StreamingEvent<Nothing>()

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

    /**
     * Get the thinking content if this is a thinking event, null otherwise
     */
    fun getThinking(): String? = when (this) {
        is Thinking -> content
        is Object -> null
    }

    /**
     * Get the object if this is an object event, null otherwise
     */
    fun getObject(): T? = when (this) {
        is Object -> item
        is Thinking -> null
    }

    /**
     * Get the thinking state if this is a thinking event, NONE otherwise
     */
    fun getThinkingState(): ThinkingState = when (this) {
        is Thinking -> state
        is Object -> ThinkingState.NONE
    }
}
