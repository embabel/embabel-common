/*
 * Copyright 2024-2026 Embabel Pty Ltd.
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
package com.embabel.common.core.thinking

/**
 * Classification of thinking content patterns for processing.
 */
enum class ThinkingTagType {
    TAG,
    PREFIX,
    NO_PREFIX
}

/**
 * Centralized definitions for thinking content patterns across different LLM providers.
 */
object ThinkingTags {

    /**
     * Comprehensive mapping of thinking tag patterns.
     */
    val TAG_DEFINITIONS = mapOf(
        "think" to ("<think>" to "</think>"),
        "analysis" to ("<analysis>" to "</analysis>"),
        "thought" to ("<thought>" to "</thought>"),
        "final" to ("<final>" to "</final>"),
        "scratchpad" to ("<scratchpad>" to "</scratchpad>"),
        "chain_of_thought" to ("<chain_of_thought>" to "</chain_of_thought>"),
        "reasoning" to ("<reasoning>" to "</reasoning>"),
        "legacy_prefix" to ("//THINKING:" to ""),
        "no_prefix" to ("" to "(?=\\{)")
    )
}
