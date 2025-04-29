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
package com.embabel.common.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.regex.Pattern

/**
 *      Tests for @see [NameGenerator]
 *     - Tests both built-in name generators (MobyNameGenerator and RandomNameGenerator)
 *     - Verifies that generated names follow expected patterns
 *     - Includes repeated tests to ensure uniqueness of generated names
 *     - Tests custom name generator implementation
 *
 */
class NameGeneratorTest {

    @Test
    fun `custom name generator should generate names`() {
        // Create a custom name generator
        val customGenerator = NameGenerator { "custom-name" }

        // Generate a name
        val name = customGenerator.generateName()

        // Verify the generated name
        assertEquals("custom-name", name)
    }

    @Test
    fun `MobyNameGenerator should generate non-empty names`() {
        // Generate a name using the MobyNameGenerator
        val name = MobyNameGenerator.generateName()

        // Verify the generated name is not empty
        assertNotNull(name)
        assertTrue(name.isNotEmpty())
    }

    @RepeatedTest(5)
    fun `MobyNameGenerator should generate different names across invocations`() {
        // Generate multiple names
        val names = mutableSetOf<String>()
        repeat(10) {
            names.add(MobyNameGenerator.generateName())
        }

        // With 10 generated names, we expect to get at least 2 different ones
        // (this test is probabilistic but extremely likely to pass)
        assertTrue(names.size > 1, "Expected multiple unique names, but got: $names")
    }

    @Test
    fun `MobyNameGenerator should follow expected pattern`() {
        // Generate names and check pattern
        val name = MobyNameGenerator.generateName()

        // MobyNamesGenerator typically generates names like "adjective_noun"
        // The exact format may vary but we can check some basic patterns
        assertTrue(name.contains("_"), "Expected name to contain underscore: $name")

        // Split the name by underscore and check that we have two parts
        val parts = name.split("_")
        assertEquals(2, parts.size, "Expected name in format 'adjective_noun': $name")
    }

    @Test
    fun `RandomNameGenerator should generate UUID strings`() {
        // Generate a name using the RandomNameGenerator
        val name = RandomNameGenerator.generateName()

        // Verify the generated name is a valid UUID
        assertNotNull(name)
        assertTrue(isValidUUID(name), "Expected UUID format but got: $name")
    }

    @RepeatedTest(5)
    fun `RandomNameGenerator should generate unique names`() {
        // Generate multiple names
        val names = mutableSetOf<String>()
        repeat(5) {
            names.add(RandomNameGenerator.generateName())
        }

        // Each UUID should be unique
        assertEquals(5, names.size, "Expected 5 unique UUIDs, but got: $names")
    }

    // Helper method to validate UUID format
    private fun isValidUUID(str: String): Boolean {
        val pattern = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        )
        return pattern.matcher(str).matches()
    }
}
