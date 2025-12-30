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
package com.embabel.common.core.thinking.spi

import com.embabel.common.core.thinking.ThinkingBlock
import com.embabel.common.core.thinking.ThinkingTagType
import com.embabel.common.core.thinking.ThinkingTags

/**
 * Extract all thinking blocks from input text.
 *
 * Processes the input text to find and extract all thinking content
 * in various formats (tagged, prefix, or untagged), returning detailed
 * metadata about each block found.
 */
@InternalThinkingApi
fun extractAllThinkingBlocks(input: String): List<ThinkingBlock> {
    val blocks = mutableListOf<ThinkingBlock>()

    // Extract thinking blocks in priority order: Tags (most common) → Prefix → No Prefix (least common)

    // 1. First: Handle both predefined and dynamic XML-style tags (most common)

    // 1a. Extract predefined tags from ThinkingTags
    ThinkingTags.TAG_DEFINITIONS.forEach { (tagKey, tagPair) ->
        if (tagKey !in listOf("legacy_prefix", "no_prefix")) {
            val (startTag, endTag) = tagPair
            if (startTag.isNotEmpty() && endTag.isNotEmpty()) {
                val escapedStart = Regex.escape(startTag)
                val escapedEnd = Regex.escape(endTag)
                val pattern = "$escapedStart(.*?)$escapedEnd".toRegex(RegexOption.DOT_MATCHES_ALL)

                pattern.findAll(input).forEach { match ->
                    blocks.add(
                        ThinkingBlock(
                            content = match.groupValues[1].trim(),
                            tagType = ThinkingTagType.TAG,
                            tagValue = tagKey
                        )
                    )
                }
            }
        }
    }

    // 1b. Extract any additional dynamic XML-style tags not in predefined list
    blocks.addAll(dynamicTagsDiscoveryAndExtraction(input, blocks))

    // 2. Second: Handle //THINKING: prefix pattern (less common)
    val prefixPattern = "//THINKING:(.*)".toRegex(RegexOption.MULTILINE)
    prefixPattern.findAll(input).forEach { match ->
        blocks.add(
            ThinkingBlock(
                content = match.groupValues[1].trim(),
                tagType = ThinkingTagType.PREFIX,
                tagValue = "THINKING"
            )
        )
    }

    // 3. Last: Handle content before JSON pattern (fallback, least common)
    // Extract any remaining content that's not inside tags or prefix lines
    var remainingInput = input

    /**
     * Remove all extracted tagged content from input to prevent NO_PREFIX false positives.
     *
     * Handles both predefined tags (using ThinkingTags definitions) and dynamic tags
     * (using standard XML patterns). This ensures that already-extracted content
     * doesn't get picked up again as untagged NO_PREFIX content.
     */
    blocks.filter { it.tagType == ThinkingTagType.TAG }.forEach { block ->
        val tagDefinition = ThinkingTags.TAG_DEFINITIONS[block.tagValue]
        if (tagDefinition != null) {
            // Remove predefined tags using their specific ThinkingTags format
            // Example: <think>content</think> or [REASONING]content[/REASONING]
            val (startTag, endTag) = tagDefinition
            val escapedStart = Regex.escape(startTag)
            val escapedEnd = Regex.escape(endTag)
            val pattern = "$escapedStart.*?$escapedEnd".toRegex(RegexOption.DOT_MATCHES_ALL)
            remainingInput = remainingInput.replace(pattern, "")
        } else {
            // Remove dynamic tags using standard XML pattern <tagname>content</tagname>
            // Escapes tag name for regex safety (e.g., "custom-tag" becomes "custom\-tag")
            // Pattern matches: <tagname optional-attrs>content</tagname>
            val escapedTagName = Regex.escape(block.tagValue)
            val pattern = "<$escapedTagName[^>]*>.*?</$escapedTagName>".toRegex(RegexOption.DOT_MATCHES_ALL)
            remainingInput = remainingInput.replace(pattern, "")
        }
    }

    // Remove all prefix lines
    remainingInput = remainingInput.replace("//THINKING:.*".toRegex(RegexOption.MULTILINE), "")

    // Extract remaining content before JSON
    val noPrefixPattern = "^(.*?)(?=\\{)".toRegex(RegexOption.DOT_MATCHES_ALL)
    noPrefixPattern.find(remainingInput.trim())?.let { match ->
        val content = skipMarkdownArtifacts(match.groupValues[1].trim())
        if (content.isNotEmpty()) {
            blocks.add(
                ThinkingBlock(
                    content = content,
                    tagType = ThinkingTagType.NO_PREFIX,
                    tagValue = ""
                )
            )
        }
    }

    return blocks.sortedBy { input.indexOf(it.content) }
}

/**
 * Remove markdown artifacts that should not be considered thinking content.
 * Currently handles ```json code fence markers that commonly appear before JSON output,
 * see get format in [[JacksonConverter]]
 */
private fun skipMarkdownArtifacts(content: String): String {
    return content
        .replace("```json", "")
        .trim()
}
