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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 *  @see [DoubleUtilsTest]: Tests the random float array generation function:
 *     - Ensuring arrays have the requested length
 *     - Verifying values are in the correct range (0 to 1)
 *     - Confirming arrays contain different values
 *     - Testing with different array lengths
 *
 */
class DoubleUtilsTest {

    @Test
    fun `generateRandomFloatArray should create array of specified length`() {
        val length = 10
        val array = generateRandomFloatArray(length)

        // Check that the array has the requested length
        assertEquals(length, array.size)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 100])
    fun `generateRandomFloatArray should work with different lengths`(length: Int) {
        val array = generateRandomFloatArray(length)

        // Check that the array has the requested length
        assertEquals(length, array.size)
    }

    @Test
    fun `generateRandomFloatArray should return values between 0 and 1`() {
        val length = 1000 // Large enough to check distribution
        val array = generateRandomFloatArray(length)

        // Check that all values are between 0.0 (inclusive) and 1.0 (exclusive)
        for (value in array) {
            assertTrue(value >= 0.0f, "Value $value should be >= 0.0")
            assertTrue(value < 1.0f, "Value $value should be < 1.0")
        }
    }

    @Test
    fun `generateRandomFloatArray should generate different values`() {
        val length = 100
        val array = generateRandomFloatArray(length)

        // Check that not all values are the same (very low probability)
        val distinctValues = array.distinct().count()
        assertTrue(distinctValues > 1, "Expected multiple distinct values, but got $distinctValues")

        // It's statistically extremely unlikely that there would be fewer than 10 distinct
        // values in 100 random floats between 0 and 1
        assertTrue(distinctValues >= 10, "Expected at least 10 distinct values, but got $distinctValues")
    }

    @Test
    fun `generateRandomFloatArray should generate different arrays on subsequent calls`() {
        val length = 20
        val array1 = generateRandomFloatArray(length)
        val array2 = generateRandomFloatArray(length)

        // Arrays should not be identical (extremely low probability)
        assertFalse(array1.contentEquals(array2), "Expected different arrays on subsequent calls")
    }
}
