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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 *      Tests for @see [Versioned]
 *     - Tests SimpleVersioned implementation
 *     - Verifies versionInfo() method returns simplified version info
 *     - Tests toString() formatting with and without an ID
 *     - Tests NoSuchVersionException formatting
 *
 */
class VersionedTest {

    @Test
    fun `SimpleVersioned should implement Versioned interface`() {
        // Create a SimpleVersioned instance
        val versioned = SimpleVersioned("test-name", 1, "test-id")

        // Check that it implements the Versioned interface
        assertTrue(versioned is Versioned)
        assertEquals("test-name", versioned.name)
        assertEquals(1, versioned.version)
        assertEquals("test-id", versioned.id)
    }

    @Test
    fun `SimpleVersioned constructor should copy from Versioned`() {
        // Create a custom Versioned implementation
        val customVersioned = object : Versioned {
            override val name = "custom-name"
            override val version = 5
            override val id = "custom-id"
        }

        // Create a SimpleVersioned from the custom Versioned
        val simple = SimpleVersioned(customVersioned)

        // Verify the values were copied correctly
        assertEquals("custom-name", simple.name)
        assertEquals(5, simple.version)
        assertEquals("custom-id", simple.id)
    }

    @Test
    fun `versionInfo should return SimpleVersioned with only version information`() {
        // Create a Versioned with additional fields
        val customVersioned = object : Versioned {
            override val name = "custom-name"
            override val version = 5
            override val id = "custom-id"

            // Additional information that shouldn't be included in versionInfo
            val extraData = "extra data"
        }

        // Get versionInfo
        val versionInfo = customVersioned.versionInfo()

        // Verify it contains only version information
        assertEquals("custom-name", versionInfo.name)
        assertEquals(5, versionInfo.version)
        assertEquals("custom-id", versionInfo.id)

        // Verify it's a SimpleVersioned instance
        assertTrue(versionInfo is SimpleVersioned)
    }

    @Test
    fun `SimpleVersioned toString should format correctly with id`() {
        val versioned = SimpleVersioned("test-name", 3, "test-id")

        val toString = versioned.toString()

        assertEquals("test-name v3 (test-id)", toString)
    }

    @Test
    fun `SimpleVersioned toString should format correctly without id`() {
        val versioned = SimpleVersioned("test-name", 3, null)

        val toString = versioned.toString()

        assertEquals("test-name v3", toString)
    }

    @Test
    fun `NoSuchVersionException should format message correctly with version`() {
        val versionSelection = VersionSelection("test-name", 2)

        val exception = NoSuchVersionException(versionSelection, "Test error")

        assertEquals("Test error: No version 2 of test-name found", exception.message)
    }

    @Test
    fun `NoSuchVersionException should format message correctly without version`() {
        val versionSelection = VersionSelection("test-name", null)

        val exception = NoSuchVersionException(versionSelection, "Test error")

        assertEquals("Test error: No version * of test-name found", exception.message)
    }
}
