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
package com.embabel.common.core.thinking.spi

import com.embabel.common.core.thinking.ThinkingBlock
import com.embabel.common.core.thinking.ThinkingTagType
import com.embabel.common.core.thinking.ThinkingTags

/**
 * Discovers and extracts dynamic XML-style thinking tags not predefined in ThinkingTags.
 *
 * Searches for valid XML tag patterns and returns thinking blocks for any
 * tags that weren't already extracted by predefined ThinkingTags processing.
 * This allows for flexible detection of new or custom thinking tags.
 *
 * @param input The text to search for dynamic tags
 * @param existingBlocks Already extracted blocks to avoid duplicates
 * @return List of ThinkingBlocks found with dynamic tags
 */
internal fun dynamicTagsDiscoveryAndExtraction(input: String, existingBlocks: List<ThinkingBlock>): List<ThinkingBlock> {
    val blocks = mutableListOf<ThinkingBlock>()

    /**
     * Build set of already extracted tag values to prevent duplicate extraction.
     * Only considers TAG type blocks since we're looking for XML-style tag conflicts.
     */
    val existingTagValues = existingBlocks
        .filter { it.tagType == ThinkingTagType.TAG }
        .map { it.tagValue }
        .toSet()

    /**
     * Find and extract dynamic XML tags using regex pattern matching.
     *
     * Regex groups:
     * - Group 1: Opening tag name (e.g., "plan" from "<plan>")
     * - Group 2: Content between opening and closing tags
     * - Group 3: Closing tag name (e.g., "plan" from "</plan>")
     *
     * Validation ensures opening/closing tags match and content exists.
     */
    DynamicTagPatterns.COMPLETE_TAG_PATTERN.findAll(input).forEach { match ->
        val openingTag = match.groupValues[1]    // Group 1: opening tag name (e.g., "analysis" from "<analysis>")
        val content = match.groupValues[2].trim()  // Group 2: content between tags
        val closingTag = match.groupValues[3]    // Group 3: closing tag name (e.g., "analysis" from "</analysis>")

        // Only add if opening/closing tags match, not already extracted, and has content
        if (openingTag == closingTag && openingTag !in existingTagValues && content.isNotEmpty()) {
            blocks.add(
                ThinkingBlock(
                    content = content,
                    tagType = ThinkingTagType.TAG,
                    tagValue = openingTag
                )
            )
        }
    }

    return blocks
}

/**
 * Static patterns for dynamic tag discovery.
 */
private object DynamicTagPatterns {
    /**
     * Pattern for complete XML tags: <tagname>content</tagname>
     * Group 1: opening tag name, Group 2: content, Group 3: closing tag name
     */
    val COMPLETE_TAG_PATTERN = "<([a-zA-Z][a-zA-Z0-9_-]*)[^>]*>(.*?)</([a-zA-Z][a-zA-Z0-9_-]*)>".toRegex(RegexOption.DOT_MATCHES_ALL)
}
