/*
* Copyright 2025 Embabel Software, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.embabel.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.converter.StructuredOutputConverter

/**
 * Utility functions for working with LLMs.
 */

private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

/**
 * Return a Kotlin aware Spring AI converter for the given class.
 * We need to call this because by default, Spring AI doesn't know how to convert Kotlin classes.
 */
fun <T> structuredOutputConverterFor(clazz: Class<T>): StructuredOutputConverter<T> = BeanOutputConverter(
    clazz,
    objectMapper,
)

/**
 * Format the given object as a few shot example.
 */
fun formatAsExample(o: Any): String {
    return objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(o)
}
