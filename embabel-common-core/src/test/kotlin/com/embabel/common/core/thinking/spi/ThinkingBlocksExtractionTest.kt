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

import com.embabel.common.core.thinking.ThinkingTagType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(InternalThinkingApi::class)
/**
 * Comprehensive tests for thinking blocks extraction functionality.
 *
 * Tests the core extraction logic for all supported thinking patterns:
 * - TAG format: <think>content</think>, <analysis>content</analysis>, etc.
 * - PREFIX format: //THINKING: content
 * - NO_PREFIX format: raw content before JSON
 * - Mixed scenarios with multiple patterns combined
 * - Edge cases and error handling
 */
class ThinkingBlocksExtractionTest {

    // ====================
    // 1. Single Format Tests
    // ====================

    @Test
    fun `should extract single TAG thinking block`() {
        // Given: Input with single <think> tag
        val input = """
            <think>
            This is a simple thinking block.
            I need to analyze the request.
            </think>

            {
                "result": "success"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract exactly one TAG block
        assertEquals(1, blocks.size)

        val thinkBlock = blocks.first()
        assertEquals(ThinkingTagType.TAG, thinkBlock.tagType)
        assertEquals("think", thinkBlock.tagValue)
        assertTrue(thinkBlock.content.contains("simple thinking block"))
        assertTrue(thinkBlock.content.contains("analyze the request"))
    }

    @Test
    fun `should extract single PREFIX thinking block`() {
        // Given: Input with single //THINKING: prefix
        val input = """
            //THINKING: I need to process this request step by step

            {
                "result": "prefix_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract exactly one PREFIX block
        assertEquals(1, blocks.size)

        val block = blocks.first()
        assertEquals(ThinkingTagType.PREFIX, block.tagType)
        assertEquals("THINKING", block.tagValue)
        assertTrue(block.content.contains("process this request step by step"))
    }

    @Test
    fun `should extract NO_PREFIX thinking content`() {
        // Given: Input with raw content before JSON
        val input = """
            This is unstructured thinking content that appears before the JSON.
            It contains reasoning and analysis without specific formatting.

            Let me think about the best approach for this task.

            {
                "result": "no_prefix_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract NO_PREFIX block
        assertEquals(1, blocks.size)

        val block = blocks.first()
        assertEquals(ThinkingTagType.NO_PREFIX, block.tagType)
        assertEquals("", block.tagValue) // Empty for NO_PREFIX
        assertTrue(block.content.contains("unstructured thinking content"))
        assertTrue(block.content.contains("best approach"))
    }

    // ====================
    // 2. Multiple Block Tests
    // ====================

    @Test
    fun `should extract multiple different TAG blocks`() {
        // Given: Input with multiple different tag types
        val input = """
            <think>
            Initial thought about the problem.
            </think>

            <analysis>
            Deep analysis of the requirements.
            </analysis>

            <thought>
            Final conclusion after analysis.
            </thought>

            {
                "result": "completed"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract all three TAG blocks
        assertEquals(3, blocks.size)

        val thinkBlock = blocks.find { it.tagValue == "think" }
        assertNotNull(thinkBlock)
        assertEquals(ThinkingTagType.TAG, thinkBlock!!.tagType)
        assertTrue(thinkBlock.content.contains("Initial thought"))

        val analysisBlock = blocks.find { it.tagValue == "analysis" }
        assertNotNull(analysisBlock)
        assertEquals(ThinkingTagType.TAG, analysisBlock!!.tagType)
        assertTrue(analysisBlock.content.contains("Deep analysis"))

        val thoughtBlock = blocks.find { it.tagValue == "thought" }
        assertNotNull(thoughtBlock)
        assertEquals(ThinkingTagType.TAG, thoughtBlock!!.tagType)
        assertTrue(thoughtBlock.content.contains("Final conclusion"))
    }

    @Test
    fun `should extract multiple PREFIX thinking blocks`() {
        // Given: Input with multiple //THINKING: lines
        val input = """
            //THINKING: First line of reasoning
            //THINKING: Second line with more details
            //THINKING: Final thoughts on the approach

            {
                "result": "prefix_format_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract all PREFIX blocks
        assertEquals(3, blocks.size)

        blocks.forEach { block ->
            assertEquals(ThinkingTagType.PREFIX, block.tagType)
            assertEquals("THINKING", block.tagValue)
            assertTrue(block.content.isNotEmpty())
        }

        val contents = blocks.map { it.content }
        assertTrue(contents.any { it.contains("First line of reasoning") })
        assertTrue(contents.any { it.contains("Second line with more details") })
        assertTrue(contents.any { it.contains("Final thoughts") })
    }

    @Test
    fun `should extract dynamic tags not in predefined list`() {
        // Given: Input with custom dynamic tags (not in ThinkingTags predefined list)
        val input = """
            <plan>
            Planning the approach step by step.
            </plan>

            <review>
            Reviewing the solution thoroughly.
            </review>

            <custom-tag>
            Custom thinking content.
            </custom-tag>

            {
                "result": "dynamic_tags_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract all dynamic tags
        assertEquals(3, blocks.size)

        blocks.forEach { block ->
            assertEquals(ThinkingTagType.TAG, block.tagType)
            assertTrue(block.content.isNotEmpty())
        }

        val tagValues = blocks.map { it.tagValue }
        assertTrue(tagValues.contains("plan"))
        assertTrue(tagValues.contains("review"))
        assertTrue(tagValues.contains("custom-tag"))
    }

    @Test
    fun `should extract mixed predefined and dynamic tags in correct priority order`() {
        // Given: Input combining predefined and dynamic tags with other formats
        val input = """
            <think>
            Primary predefined thinking block.
            </think>

            <plan>
            Dynamic plan tag.
            </plan>

            //THINKING: Prefix style reasoning line
            //THINKING: Another prefix thought

            <custom-dynamic>
            Custom dynamic tag content.
            </custom-dynamic>

            Raw untagged content before JSON.
            This should be detected as NO_PREFIX.

            {
                "result": "mixed_format_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract blocks from all formats
        assertTrue(blocks.size >= 5)

        // Verify TAG blocks (predefined + dynamic)
        val tagBlocks = blocks.filter { it.tagType == ThinkingTagType.TAG }
        assertEquals(3, tagBlocks.size)
        assertTrue(tagBlocks.any { it.tagValue == "think" && it.content.contains("Primary predefined") })
        assertTrue(tagBlocks.any { it.tagValue == "plan" && it.content.contains("Dynamic plan") })
        assertTrue(tagBlocks.any { it.tagValue == "custom-dynamic" && it.content.contains("Custom dynamic tag") })

        // Verify PREFIX blocks
        val prefixBlocks = blocks.filter { it.tagType == ThinkingTagType.PREFIX }
        assertEquals(2, prefixBlocks.size)
        assertTrue(prefixBlocks.all { it.tagValue == "THINKING" })
        assertTrue(prefixBlocks.any { it.content.contains("Prefix style reasoning") })
        assertTrue(prefixBlocks.any { it.content.contains("Another prefix thought") })

        // Verify NO_PREFIX block
        val noPrefixBlocks = blocks.filter { it.tagType == ThinkingTagType.NO_PREFIX }
        assertEquals(1, noPrefixBlocks.size)
        assertTrue(noPrefixBlocks.first().content.contains("Raw untagged content"))
    }

    // ====================
    // 3. Edge Cases
    // ====================

    @Test
    fun `should handle empty input gracefully`() {
        // Given: Empty input
        val input = ""

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should return empty list
        assertEquals(0, blocks.size)
    }

    @Test
    fun `should handle input with only JSON`() {
        // Given: Input with only JSON, no thinking content
        val input = """
            {
                "result": "no_thinking_content"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should return empty list
        assertEquals(0, blocks.size)
    }

    @Test
    fun `should handle clean JSON input without thinking blocks`() {
        // Given: Pure JSON with no thinking content
        val input = """
            {
                "result": "pure_json_test",
                "value": 42,
                "status": "success"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should return empty list for pure JSON
        assertEquals(0, blocks.size)
    }

    @Test
    fun `should handle malformed tags gracefully`() {
        // Given: Input with malformed/unclosed tags
        val input = """
            <think>
            This tag is not properly closed
            </thinking>

            <analysis>
            This tag is never closed

            Raw content that should be detected.

            {
                "result": "malformed_tags_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should handle gracefully - malformed content treated as NO_PREFIX
        val noPrefixBlocks = blocks.filter { it.tagType == ThinkingTagType.NO_PREFIX }
        assertTrue(noPrefixBlocks.isNotEmpty())
        assertTrue(noPrefixBlocks.any { it.content.contains("Raw content") })
    }

    @Test
    fun `should treat malformed tags as NO_PREFIX content`() {
        // Given: Input with malformed thinking tags (wrong closing tags and unclosed tags)
        val input = """
            <think>
            This tag has wrong closing tag
            </thinking>

            <analysis>
            This tag is never closed and should be treated as NO_PREFIX content

            {
                "result": "malformed_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Malformed content should be treated as NO_PREFIX
        val noPrefixBlocks = blocks.filter { it.tagType == ThinkingTagType.NO_PREFIX }
        assertTrue(noPrefixBlocks.isNotEmpty())

        // Verify malformed tags are captured in NO_PREFIX content
        val noPrefixContent = noPrefixBlocks.map { it.content }.joinToString(" ")
        assertTrue(noPrefixContent.contains("<think>") || noPrefixContent.contains("<analysis>"))
        assertTrue(noPrefixContent.contains("wrong closing tag") || noPrefixContent.contains("never closed"))
    }

    @Test
    fun `should handle nested tags correctly`() {
        // Given: Input with nested tag structures
        val input = """
            <think>
            Outer thinking content
            <inner>Nested content that should be included</inner>
            More outer content
            </think>

            {
                "result": "nested_tags_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should extract outer tag content including nested content
        assertEquals(1, blocks.size)

        val block = blocks.first()
        assertEquals(ThinkingTagType.TAG, block.tagType)
        assertEquals("think", block.tagValue)
        assertTrue(block.content.contains("Outer thinking content"))
        assertTrue(block.content.contains("<inner>Nested content"))
        assertTrue(block.content.contains("More outer content"))
    }

    @Test
    fun `should handle whitespace and formatting correctly`() {
        // Given: Input with various whitespace patterns
        val input = """
            <think>
                Indented content with spaces
                    Multiple levels of indentation
                Trailing spaces
            </think>

            {
                "result": "whitespace_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should preserve internal formatting but trim outer whitespace
        assertEquals(1, blocks.size)

        val block = blocks.first()
        assertEquals("think", block.tagValue)
        assertTrue(block.content.contains("Indented content"))
        assertTrue(block.content.contains("Multiple levels"))
        // Content should be trimmed of leading/trailing whitespace
        assertTrue(block.content.trim().isNotEmpty())
    }

    // ====================
    // 4. Order & Priority
    // ====================

    @Test
    fun `should preserve content order in result`() {
        // Given: Input with blocks in specific order
        val input = """
            <think>First block content</think>
            //THINKING: Second block content
            <analysis>Third block content</analysis>

            {
                "result": "order_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should preserve content order based on appearance in input
        assertEquals(3, blocks.size)

        // Verify blocks appear in order based on their position in input
        val firstThink = blocks.find { it.content.contains("First block") }
        val secondThinking = blocks.find { it.content.contains("Second block") }
        val thirdAnalysis = blocks.find { it.content.contains("Third block") }

        assertNotNull(firstThink)
        assertNotNull(secondThinking)
        assertNotNull(thirdAnalysis)
    }

    // ====================
    // 5. Content Validation
    // ====================

    @Test
    fun `should have correct tag metadata for all formats`() {
        // Given: Input with all three formats
        val input = """
            <think>TAG format content</think>
            //THINKING: PREFIX format content
            NO_PREFIX format content here.

            {
                "result": "metadata_test"
            }
        """.trimIndent()

        // When: Extract thinking blocks
        val blocks = extractAllThinkingBlocks(input)

        // Then: Should have correct metadata for each format
        assertEquals(3, blocks.size)

        // TAG format validation
        val tagBlock = blocks.find { it.tagType == ThinkingTagType.TAG }
        assertNotNull(tagBlock)
        assertEquals("think", tagBlock.tagValue)
        assertTrue(tagBlock.content.contains("TAG format content"))

        // PREFIX format validation
        val prefixBlock = blocks.find { it.tagType == ThinkingTagType.PREFIX }
        assertNotNull(prefixBlock)
        assertEquals("THINKING", prefixBlock.tagValue)
        assertTrue(prefixBlock.content.contains("PREFIX format content"))

        // NO_PREFIX format validation
        val noPrefixBlock = blocks.find { it.tagType == ThinkingTagType.NO_PREFIX }
        assertNotNull(noPrefixBlock)
        assertEquals("", noPrefixBlock.tagValue) // Empty for NO_PREFIX
        assertTrue(noPrefixBlock.content.contains("NO_PREFIX format content"))
    }
}
