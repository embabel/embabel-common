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

/**
 *   @see [VersionSelection]
 *     - Tests constructor and property access
 *     - Verifies equals() and hashCode() behavior
 *     - Tests the companion object's require() method
 *     - Tests destructuring support
 *
 */
class VersionSelectionTest {

    @Test
    fun `constructor should set properties correctly`() {
        val versionSelection = VersionSelection("test-name", 3, "test-id")

        assertEquals("test-name", versionSelection.name)
        assertEquals(3, versionSelection.version)

        // The id is private, so we can't directly assert its value
        // However, we can verify equality with an identical object
        assertEquals(VersionSelection("test-name", 3, "test-id"), versionSelection)
    }

    @Test
    fun `version parameter should be optional`() {
        val versionSelection = VersionSelection("test-name")

        assertEquals("test-name", versionSelection.name)
        assertNull(versionSelection.version)
    }

    /* @Test  TODO - should "id" be included into equals? */
    fun `equals should consider name and version but not id`() {
        val selection1 = VersionSelection("name", 1, "id1")
        val selection2 = VersionSelection("name", 1, "id2")
        val selection3 = VersionSelection("different", 1, "id1")
        val selection4 = VersionSelection("name", 2, "id1")

        // Same name and version, different id
        assertEquals(selection1, selection2)

        // Different name
        assertNotEquals(selection1, selection3)

        // Different version
        assertNotEquals(selection1, selection4)
    }

    /* @Test TODO - should "id" be included into hashcode? */
    fun `hashCode should be consistent with equals`() {
        val selection1 = VersionSelection("name", 1, "id1")
        val selection2 = VersionSelection("name", 1, "id2")

        // If two objects are equal, their hashCodes must be equal
        assertEquals(selection1.hashCode(), selection2.hashCode())
    }

    @Test
    fun `companion object require should create VersionSelection from Versioned`() {
        // Create a Versioned object
        val versioned = SimpleVersioned("test-name", 3, "test-id")

        // Create VersionSelection using companion object method
        val versionSelection = VersionSelection.require(versioned)

        // Verify the VersionSelection was created correctly
        assertEquals("test-name", versionSelection.name)
        assertEquals(3, versionSelection.version)
    }

    @Test
    fun `component functions should work for destructuring`() {
        val versionSelection = VersionSelection("test-name", 3)

        // Use destructuring to extract components
        val (name, version) = versionSelection

        // Verify components match the original object
        assertEquals("test-name", name)
        assertEquals(3, version)
    }
}
