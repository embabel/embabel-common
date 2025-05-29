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
package com.embabel.common.core.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

internal class StringTrimmingUtilsTest {

    @Nested
    internal inner class Trim {
        @Nested
        internal inner class TestInvalidArgs {
            @Test
            fun testMaxLessThanKeepRight() {
                Assertions.assertThrows<java.lang.IllegalArgumentException?>(
                    java.lang.IllegalArgumentException::class.java,
                    Executable { trim("foo", 10, 11) })
            }

            @Test
            fun testMaxLessThanEllipsis() {
                Assertions.assertThrows<java.lang.IllegalArgumentException?>(
                    IllegalArgumentException::class.java,
                    Executable { trim("foo", 2, 1, "weroiwueoiruwoeiruowieurowieuruwieur") })
            }
        }

        @Nested
        internal inner class TestValidArgs {
            @Test
            fun testNullReturnsNull() {
                Assertions.assertNull(trim(null, 100, 12))
            }

            @Test
            fun testShortString() {
                val shortString = "werwerewsr"
                Assertions.assertEquals(shortString, trim(shortString, 100, 12))
            }

            @Test
            fun testLongString() {
                val shortString = "werwerewsrwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwXX"
                val trimmed = trim(shortString, 10, 2)
                Assertions.assertEquals(10, trimmed!!.length)
                Assertions.assertTrue(trimmed.endsWith("XX"))
                Assertions.assertTrue(trimmed.contains(DEFAULT_ELLIPSIS))
            }

            @Test
            fun testLongStringWithZeroKeepRight() {
                val shortString = "werwerewsrwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwXX"
                val trimmed = trim(shortString, 10, 0)
                Assertions.assertEquals(10, trimmed!!.length)
                Assertions.assertFalse(trimmed.endsWith("XX"))
                Assertions.assertTrue(trimmed.contains(DEFAULT_ELLIPSIS))
            }

            @Test
            fun customEllipsis() {
                val shortString = "werwerewsrwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwerwXX"
                val customEllipsis = "custom-ellipsis"
                val trimmed = trim(shortString, 50, 12, customEllipsis)
                Assertions.assertEquals(50, trimmed!!.length)
                Assertions.assertTrue(trimmed.endsWith("XX"))
                Assertions.assertTrue(trimmed.contains(customEllipsis))
            }
        }
    }

    @Nested
    internal inner class RemoveWhitespace {
        @Test
        fun testNull() {
            Assertions.assertNull(removeWhitespace(null))
        }

        @Test
        fun testEmptyString() {
            val s = ""
            val expected = ""
            Assertions.assertEquals(expected, removeWhitespace(s))
        }

        @Test
        fun testRemoveWhitespace() {
            val s = "  a  b  c  "
            val expected = "abc"
            Assertions.assertEquals(expected, removeWhitespace(s))
        }
    }
}
