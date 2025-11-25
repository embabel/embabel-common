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

import com.embabel.common.ai.converters.FilteringJacksonOutputConverter
import com.embabel.common.core.streaming.StreamingEvent
import com.embabel.common.core.streaming.StreamingUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.ParameterizedTypeReference
import reactor.core.publisher.Flux
import java.util.function.Predicate

/**
 * Streaming output converter that extends FilteringJacksonOutputConverter to support JSONL format.
 *
 * This converter enables streaming LLM responses by:
 * - Converting JSONL (JSON Lines) input to reactive Flux<T> streams of typed objects
 * - Supporting mixed content streams with both objects and thinking content via StreamingEvent<T>
 * - Inheriting JSON schema injection and property filtering capabilities from parent FilteringJacksonOutputConverter
 * - Providing instructions to LLMs for proper JSONL output formatting
 *
 * Use cases:
 * - Streaming lists of objects from LLM responses in real-time
 * - Processing LLM reasoning (thinking) alongside structured outputs
 * - Real-time agent progress monitoring and incremental results
 *
 * The converter requests JSONL format from LLMs and parses each line as a separate
 * JSON object, emitting them as reactive stream events as they become available.
 */
class StreamingJacksonOutputConverter<T> : FilteringJacksonOutputConverter<T> {

    constructor(
        clazz: Class<T>,
        objectMapper: ObjectMapper,
        propertyFilter: Predicate<String> = Predicate { true },
    ) : super(clazz, objectMapper, propertyFilter)

    constructor(
        typeReference: ParameterizedTypeReference<T>,
        objectMapper: ObjectMapper,
        propertyFilter: Predicate<String> = Predicate { true },
    ) : super(typeReference, objectMapper, propertyFilter)

    /**
     * Convert streaming JSONL text to a Flux of typed objects.
     * Each line should be a valid JSON object matching the schema.
     * Uses resilient error handling - logs warnings for null conversions but continues processing other lines.
     */
    fun convertStream(jsonlContent: String): Flux<T> {
        return convertStreamWithThinking(jsonlContent)
            .filter { event -> event.isObject() }
            .map { event -> event.getObject()!! }
    }


    /**
     * Convert streaming text with thinking blocks into StreamingEvent objects.
     * Supports both object lines and thinking blocks.
     * Uses resilient error handling - logs warnings for individual line failures but continues processing.
     */
    fun convertStreamWithThinking(text: String): Flux<StreamingEvent<T>> {
        return Flux.fromIterable(text.lines())
            .filter { it.isNotBlank() }
            .handle { line, sink ->
                try {
                    when {
                        StreamingUtils.isThinkingLine(line) -> {
                            val thinkingContent = StreamingUtils.extractThinkingContent(line)
                            sink.next(StreamingEvent.Thinking(thinkingContent))
                        }
                        else -> {
                            val result = super.convert(line)
                            if (result != null) {
                                sink.next(StreamingEvent.Object(result))
                            // Continue processing other lines
                            }
                        }
                    }
                } catch (e: Exception) {
                    sink.error(e)
                }
            }
    }

    /**
     * Override format to request JSONL instead of single JSON.
     * Inherits schema injection from parent but modifies instructions for streaming.
     */
    override fun getFormat(): String =
        """|
           |Your response should be in JSONL (JSON Lines) format.
           |Each line should contain exactly one JSON object matching the schema.
           |Do not include explanations, only RFC7464 compliant JSON Lines, one per line.
           |Do not include markdown code blocks or wrap in arrays.
           |
           |You may include thinking content ANYWHERE in your response using:
           |<think>your reasoning here</think>
           |
           |Thinking blocks can appear before, between, or after JSON objects as needed for your reasoning process.
           |
           |JSON Schema for each object line:
           |```${jsonSchema}```
           |
           |Example output showing flexible thinking placement:
           |<think>Let me analyze this step by step</think>
           |{"field1": "value1", "field2": "value2"}
           |<think>The next item requires different consideration</think>
           |{"field1": "value3", "field2": "value4"}
           |<think>Final thoughts on the results</think>
           |{"field1": "value5", "field2": "value6"}
           |""".trimMargin()
}
