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
 * Utility functions for streaming content processing, particularly thinking content detection and extraction.
 *
 * Provides centralized logic for identifying and processing thinking content in various formats
 * used by different LLM models and reasoning systems.
 */
object StreamingUtils {

    /**
     * Detects if a line contains thinking content using flexible pattern matching.
     *
     * Supports multiple reasoning tag formats commonly used by different LLMs:
     * - <think>content</think> (DeepSeek, Qwen, Llama 3, Gemma)
     * - <analysis>content</analysis> (Qwen)
     * - <thought>content</thought> (Llama 3)
     * - <final>content</final> (Qwen)
     * - <scratchpad>content</scratchpad> (Gemini internal)
     * - <chain_of_thought>content</chain_of_thought> (Claude internal)
     * - [REASONING]content[/REASONING] (Mistral/Mixtral)
     * - //THINKING: content (legacy format)
     *
     * @param line The complete line to check for thinking patterns
     * @return true if the line contains thinking content, false otherwise
     */
    fun isThinkingLine(line: String): Boolean {
        return thinkingPatterns.any { pattern ->
            pattern.containsMatchIn(line)
        }
    }

    /**
     * Extracts thinking content from a line, removing the markup tags.
     *
     * This method finds the first matching thinking pattern and extracts the content
     * while removing the surrounding markup tags or prefixes.
     *
     * @param line The complete line containing thinking markup
     * @return The extracted thinking content without markup, or the original line if no pattern matches
     */
    fun extractThinkingContent(line: String): String {
        // Try each pattern to find and extract content
        for (pattern in thinkingPatterns) {
            val match = pattern.find(line)
            if (match != null) {
                // For block patterns like <think>content</think>, extract group 1
                if (match.groupValues.size > 1) {
                    return match.groupValues[1].trim()
                }
                // For prefix patterns like //THINKING: content, remove the prefix
                if (line.startsWith("//THINKING:")) {
                    return line.removePrefix("//THINKING:").trim()
                }
                //  continue to next pattern
            }
        }
        // Fallback: return the line as-is if no pattern matches
        return line.trim()
    }

    /**
         * Regex patterns for detecting thinking content in various formats.
         * Ordered from most specific to most general for optimal matching performance.
         *
         * Based on research of common reasoning tag formats used across different LLM families:
         * - Block-style tags with content capture groups
         * - Prefix-style markers for legacy compatibility
         */
        private val thinkingPatterns = listOf(
            // Block-style thinking tags (capture content inside)
            "<think>(.*?)</think>".toRegex(RegexOption.DOT_MATCHES_ALL),
            "<analysis>(.*?)</analysis>".toRegex(RegexOption.DOT_MATCHES_ALL),
            "<thought>(.*?)</thought>".toRegex(RegexOption.DOT_MATCHES_ALL),
            "<final>(.*?)</final>".toRegex(RegexOption.DOT_MATCHES_ALL),
            "<scratchpad>(.*?)</scratchpad>".toRegex(RegexOption.DOT_MATCHES_ALL),
            "<chain_of_thought>(.*?)</chain_of_thought>".toRegex(RegexOption.DOT_MATCHES_ALL),
            "\\[REASONING\\](.*?)\\[/REASONING\\]".toRegex(RegexOption.DOT_MATCHES_ALL),

            // Prefix-style thinking markers (for legacy compatibility)
            "^//THINKING:.*".toRegex(RegexOption.MULTILINE)
        )
}
