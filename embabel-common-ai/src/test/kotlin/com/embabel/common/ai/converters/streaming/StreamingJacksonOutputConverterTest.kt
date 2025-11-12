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
package com.embabel.common.ai.converters.streaming

import com.embabel.common.core.streaming.StreamingEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class StreamingJacksonOutputConverterTest {

    private val objectMapper = jacksonObjectMapper()

    data class SimpleItem(val name: String)

    @Test
    fun `getFormat should request JSONL format instead of single JSON`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)

        // When
        val format = converter.getFormat()

        // Then
        assertTrue(format.contains("JSONL (JSON Lines) format"))
        assertTrue(format.contains("Each line should contain exactly one JSON object"))
        assertTrue(format.contains("wrap in arrays"))
        assertTrue(format.contains("//THINKING:"))
    }

    @Test
    fun `getFormat should inherit schema from parent JacksonOutputConverter`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)

        // When
        val format = converter.getFormat()

        // Then
        assertTrue(format.contains("JSON Schema"))
        assertTrue(format.contains("name")) // Should contain schema for SimpleItem
    }

    @Test
    fun `convertStream should handle empty input gracefully`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)

        // When
        val result = converter.convertStream("")

        // Then
        val items = result.collectList().block()
        assertNotNull(items)
        assertTrue(items!!.isEmpty())
    }

    @Test
    fun `convertStream should filter blank lines`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val jsonlWithBlanks = """
            {"name": "item1"}

            {"name": "item2"}

        """.trimIndent()

        // When
        val result = converter.convertStream(jsonlWithBlanks)

        // Then
        val items = result.collectList().block()
        assertNotNull(items)
        assertEquals(2, items!!.size)
    }

    @Test
    fun `convertStream should delegate to parent convert method for each line`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val validJsonl = """
            {"name": "test1"}
            {"name": "test2"}
        """.trimIndent()

        // When
        val result = converter.convertStream(validJsonl)

        // Then
        val items = result.collectList().block()
        assertNotNull(items)
        assertEquals(2, items!!.size)
        assertEquals("test1", items[0].name)
        assertEquals("test2", items[1].name)
    }

    @Test
    fun `convertStreamWithThinking should parse thinking lines correctly`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val mixedContent = """
            //THINKING: analyzing requirement
            {"name": "result"}
        """.trimIndent()

        // When
        val result = converter.convertStreamWithThinking(mixedContent)

        // Then
        val events = result.collectList().block()
        assertNotNull(events)
        assertEquals(2, events!!.size)

        assertTrue(events[0] is StreamingEvent.Thinking)
        assertEquals("analyzing requirement", (events[0] as StreamingEvent.Thinking).content)

        assertTrue(events[1] is StreamingEvent.Object<*>)
        assertEquals("result", (events[1] as StreamingEvent.Object<SimpleItem>).item.name)
    }

    @Test
    fun `convertStreamWithThinking should handle mixed content with multiple thinking blocks`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val content = """
            //THINKING: step 1
            //THINKING: step 2
            {"name": "final"}
        """.trimIndent()

        // When
        val result = converter.convertStreamWithThinking(content)

        // Then
        val events = result.collectList().block()
        assertNotNull(events)
        assertEquals(3, events!!.size) // 2 thinking + 1 object
    }

    @Test
    fun `convertStreamWithThinking should strip THINKING prefix correctly`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val thinkingLine = "//THINKING: detailed analysis here"

        // When
        val result = converter.convertStreamWithThinking(thinkingLine)

        // Then
        val events = result.collectList().block()
        assertNotNull(events)
        assertEquals(1, events!!.size)

        assertTrue(events[0] is StreamingEvent.Thinking)
        assertEquals("detailed analysis here", (events[0] as StreamingEvent.Thinking).content)
    }

    @Test
    fun `convertStreamWithThinking should delegate object parsing to parent`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val objectLine = """{"name": "testItem"}"""

        // When
        val result = converter.convertStreamWithThinking(objectLine)

        // Then
        val events = result.collectList().block()
        assertNotNull(events)
        assertEquals(1, events!!.size)

        assertTrue(events[0] is StreamingEvent.Object<*>)
        assertEquals("testItem", (events[0] as StreamingEvent.Object<SimpleItem>).item.name)
    }

    @Test
    fun `convertStream should fail on malformed JSON`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val invalidJson = "invalid json line"

        // When & Then - Should error on malformed JSON
        assertThrows<IllegalArgumentException> {
            converter.convertStream(invalidJson).collectList().block()
        }
    }

    @Test
    fun `convertStreamWithThinking should fail on malformed JSON in mixed content`() {
        // Given
        val converter = StreamingJacksonOutputConverter(SimpleItem::class.java, objectMapper)
        val mixedContent = """
            //THINKING: this is fine
            invalid json here
        """.trimIndent()

        // When & Then - Should error on malformed JSON
        assertThrows<IllegalArgumentException> {
            converter.convertStreamWithThinking(mixedContent).collectList().block()
        }
    }
}
