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
 * Represents a thinking block extracted from LLM output.
 *
 * This class encapsulates thinking content that LLMs generate as part of their
 * reasoning process, along with metadata about the format used.
 *
 * @property content The extracted thinking text with all markup removed
 * @property tagType The type of thinking pattern - see [ThinkingTagType] for available types
 * @property tagValue The specific tag identifier used (e.g., "think", "analysis", "THINKING")
 *
 * @see ThinkingTagType for the different pattern classifications
 * @see ThinkingTags.TAG_DEFINITIONS for supported tag formats
 */
data class ThinkingBlock(
    /**
     * The extracted thinking content with all markup tags removed.
     * Contains only the inner reasoning text.
     */
    val content: String,

    /**
     * The type of thinking pattern that was detected.
     * @see ThinkingTagType
     */
    val tagType: ThinkingTagType,

    /**
     * The specific tag identifier that was used.
     * For [ThinkingTagType.TAG]: the tag name (e.g., "think", "analysis")
     * For [ThinkingTagType.PREFIX]: the prefix identifier (e.g., "THINKING")
     * For [ThinkingTagType.NO_PREFIX]: empty string
     */
    val tagValue: String
)
