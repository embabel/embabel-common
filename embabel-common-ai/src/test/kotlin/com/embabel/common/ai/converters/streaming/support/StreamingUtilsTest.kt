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
package com.embabel.common.ai.converters.streaming.support

import com.embabel.common.core.streaming.ThinkingState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StreamingUtilsTest {

    @Test
    fun `isThinkingLine should detect standard thinking tags`() {
        assertTrue(StreamingUtils.isThinkingLine("<think>content</think>"))
        assertTrue(StreamingUtils.isThinkingLine("<analysis>content</analysis>"))
        assertTrue(StreamingUtils.isThinkingLine("<thought>content</thought>"))
        assertTrue(StreamingUtils.isThinkingLine("<final>content</final>"))
        assertTrue(StreamingUtils.isThinkingLine("<scratchpad>content</scratchpad>"))
        assertTrue(StreamingUtils.isThinkingLine("<chain_of_thought>content</chain_of_thought>"))
        assertTrue(StreamingUtils.isThinkingLine("[REASONING]content[/REASONING]"))
        assertTrue(StreamingUtils.isThinkingLine("//THINKING: content"))
    }

    @Test
    fun `isThinkingLine should not detect JSON or regular text`() {
        assertFalse(StreamingUtils.isThinkingLine("{\"name\": \"value\"}"))
        assertFalse(StreamingUtils.isThinkingLine("regular text line"))
        assertFalse(StreamingUtils.isThinkingLine(""))
        assertFalse(StreamingUtils.isThinkingLine("random <tag> not thinking"))
    }

    @Test
    fun `extractThinkingContent should extract content from thinking tags`() {
        Assertions.assertEquals("content", StreamingUtils.extractThinkingContent("<think>content</think>"))
        Assertions.assertEquals(
            "analysis content",
            StreamingUtils.extractThinkingContent("<analysis>analysis content</analysis>")
        )
        Assertions.assertEquals(
            "thought content",
            StreamingUtils.extractThinkingContent("<thought>thought content</thought>")
        )
        Assertions.assertEquals(
            "mistral reasoning",
            StreamingUtils.extractThinkingContent("[REASONING]mistral reasoning[/REASONING]")
        )
        Assertions.assertEquals("legacy thinking", StreamingUtils.extractThinkingContent("//THINKING: legacy thinking"))
    }

    @Test
    fun `extractThinkingContent should handle multiline content`() {
        val multilineContent = "<think>line 1\nline 2\nline 3</think>"
        Assertions.assertEquals("line 1\nline 2\nline 3", StreamingUtils.extractThinkingContent(multilineContent))
    }

    @Test
    fun `extractThinkingContent should trim whitespace`() {
        Assertions.assertEquals("content", StreamingUtils.extractThinkingContent("<think>  content  </think>"))
        Assertions.assertEquals("thinking", StreamingUtils.extractThinkingContent("//THINKING:   thinking   "))
    }

    @Test
    fun `extractThinkingContent should return original line if no pattern matches`() {
        val nonThinkingLine = "regular text"
        Assertions.assertEquals("regular text", StreamingUtils.extractThinkingContent(nonThinkingLine))
    }

    @Test
    fun `detectThinkingState should return BOTH for complete single-line blocks`() {
        Assertions.assertEquals(
            ThinkingState.BOTH,
            StreamingUtils.detectThinkingState("<think>complete thought</think>")
        )
        Assertions.assertEquals(
            ThinkingState.BOTH,
            StreamingUtils.detectThinkingState("<analysis>complete analysis</analysis>")
        )
        Assertions.assertEquals(
            ThinkingState.BOTH,
            StreamingUtils.detectThinkingState("[REASONING]complete reasoning[/REASONING]")
        )
        Assertions.assertEquals(
            ThinkingState.BOTH,
            StreamingUtils.detectThinkingState("//THINKING: complete legacy thought")
        )
    }

    @Test
    fun `detectThinkingState should return START for opening tags only`() {
        Assertions.assertEquals(ThinkingState.START, StreamingUtils.detectThinkingState("<think>"))
        Assertions.assertEquals(ThinkingState.START, StreamingUtils.detectThinkingState("<analysis>"))
        Assertions.assertEquals(ThinkingState.START, StreamingUtils.detectThinkingState("[REASONING]"))
    }

    @Test
    fun `detectThinkingState should return END for closing tags only`() {
        Assertions.assertEquals(ThinkingState.END, StreamingUtils.detectThinkingState("</think>"))
        Assertions.assertEquals(ThinkingState.END, StreamingUtils.detectThinkingState("</analysis>"))
        Assertions.assertEquals(ThinkingState.END, StreamingUtils.detectThinkingState("[/REASONING]"))
    }

    @Test
    fun `detectThinkingState should return START for opening tag with content but no closing`() {
        Assertions.assertEquals(ThinkingState.START, StreamingUtils.detectThinkingState("<think>starting to think"))
        Assertions.assertEquals(ThinkingState.START, StreamingUtils.detectThinkingState("<analysis>beginning analysis"))
    }

    @Test
    fun `detectThinkingState should return END for content with closing tag`() {
        Assertions.assertEquals(ThinkingState.END, StreamingUtils.detectThinkingState("ending thought</think>"))
        Assertions.assertEquals(ThinkingState.END, StreamingUtils.detectThinkingState("final analysis</analysis>"))
    }

    @Test
    fun `detectThinkingState should return NONE for valid JSON`() {
        Assertions.assertEquals(ThinkingState.NONE, StreamingUtils.detectThinkingState("{\"name\": \"value\"}"))
        Assertions.assertEquals(ThinkingState.NONE, StreamingUtils.detectThinkingState("{\"field\": 123}"))
        Assertions.assertEquals(
            ThinkingState.NONE,
            StreamingUtils.detectThinkingState("{\"complex\": {\"nested\": true}}")
        )
    }

    @Test
    fun `detectThinkingState should return CONTINUATION for non-JSON text`() {
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("regular text line"))
        Assertions.assertEquals(
            ThinkingState.CONTINUATION,
            StreamingUtils.detectThinkingState("continuing the thought")
        )
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("invalid json { broken"))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("partial json }"))
    }

    @Test
    fun `isValidJson should identify valid JSON objects`() {
        assertTrue(StreamingUtils.isValidJson("{\"name\": \"value\"}"))
        assertTrue(StreamingUtils.isValidJson("{\"field\": 123, \"other\": true}"))
        assertTrue(StreamingUtils.isValidJson("  {\"trimmed\": true}  "))
    }

    @Test
    fun `isValidJson should reject invalid JSON formats`() {
        assertFalse(StreamingUtils.isValidJson("not json"))
        assertFalse(StreamingUtils.isValidJson("[\"array\", \"not\", \"object\"]"))
        assertFalse(StreamingUtils.isValidJson("{\"incomplete\": "))
        assertFalse(StreamingUtils.isValidJson("incomplete\": \"value\"}"))
        assertFalse(StreamingUtils.isValidJson("\"just a string\""))
        assertFalse(StreamingUtils.isValidJson("123"))
        assertFalse(StreamingUtils.isValidJson("true"))
        assertFalse(StreamingUtils.isValidJson(""))
        assertFalse(StreamingUtils.isValidJson("   "))
        assertFalse(StreamingUtils.isValidJson("{}"))  // No colon = not valid JSON object
    }

    @Test
    fun `isValidJson should be fast heuristic, not perfect parser`() {
        // This test documents that isValidJson() is a fast heuristic, not perfect
        // It may have false positives, but actual JSON parsing will catch real errors
        assertTrue(StreamingUtils.isValidJson("{malformed: but has colon}"))
        assertTrue(StreamingUtils.isValidJson("{\"unclosed\": \"string}"))

        // But should reject obvious non-JSON
        assertFalse(StreamingUtils.isValidJson("{no colon here}"))
        assertFalse(StreamingUtils.isValidJson("{malformed but looks like object}"))
    }

    @Test
    fun `detectThinkingState should handle complex multi-line scenarios`() {
        // Multi-line block start
        Assertions.assertEquals(ThinkingState.START, StreamingUtils.detectThinkingState("<think>"))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("This is line 1"))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("This is line 2"))
        Assertions.assertEquals(ThinkingState.END, StreamingUtils.detectThinkingState("</think>"))

        // Mixed content
        Assertions.assertEquals(ThinkingState.NONE, StreamingUtils.detectThinkingState("{\"after\": \"thinking\"}"))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("some more thoughts"))
    }

    @Test
    fun `detectThinkingState should handle edge cases`() {
        // Empty lines
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState(""))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("   "))

        // Lines with only partial JSON
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("{"))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("}"))
        Assertions.assertEquals(ThinkingState.CONTINUATION, StreamingUtils.detectThinkingState("{\"incomplete"))
    }
}
