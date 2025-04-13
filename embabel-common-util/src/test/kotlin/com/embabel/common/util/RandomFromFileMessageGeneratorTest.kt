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
package com.embabel.common.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class RandomFromFileMessageGeneratorTest {

    @Test
    fun `test generate`() {
        val generator = RandomFromFileMessageGenerator("classpath:messages.txt")
        for (i in 0..100) {
            val message = generator.generate()
            Assertions.assertNotNull(message)
            assertFalse(message.startsWith("#"))
            assertTrue(message.isNotBlank())
        }
    }

    @Test
    fun `test list`() {
        val messages = RandomFromFileMessageGenerator("classpath:messages.txt").messages
        messages.forEach { message ->
            Assertions.assertNotNull(message)
            assertFalse(message.startsWith("#"))
            assertTrue(message.isNotBlank())
        }
    }

    @Test
    fun `test multiline`() {
        val generator = RandomFromFileMessageGenerator("classpath:messages.txt")
        assertTrue(
            generator.messages.contains(
                """This is a multiline message
that preserves newlines
   and indentation
within the triple quotes""".trimIndent(),
            ),
            "Multiline message not found in the list:\n${generator.messages.joinToString("\n") { "<$it>" }}"
        )
    }

}
