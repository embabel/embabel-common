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
package com.embabel.common.util

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DummyInstanceCreatorTest {
    private val dummyInstanceCreator = DummyInstanceCreator()

    @Nested
    inner class `Primitive Types` {

        @Test
        fun `should create dummy String`() {
            val result = dummyInstanceCreator.createDummyInstance(String::class.java)
            assertTrue(result is String)
            assertNotNull(result)
        }

        @Test
        fun `should create dummy Int`() {
            val result = dummyInstanceCreator.createDummyInstance(Int::class.java)
            assertTrue(result is Int)
        }

        @Test
        fun `should create dummy Long`() {
            val result = dummyInstanceCreator.createDummyInstance(Long::class.java)
            assertTrue(result is Long)
        }

        @Test
        fun `should create dummy Boolean`() {
            val result = dummyInstanceCreator.createDummyInstance(Boolean::class.java)
            assertTrue(result is Boolean)
        }
    }

    @Nested
    inner class `Complex Types` {

        @Test
        fun `should create dummy List`() {
            val result = dummyInstanceCreator.createDummyInstance(List::class.java)
            assertTrue(result is List<*>)
            assertEquals(1, result.size)
        }
    }
}
