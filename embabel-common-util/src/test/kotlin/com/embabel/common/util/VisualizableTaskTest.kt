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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @see [VisualizableTaskTest]: Tests the progress bar creation functionality:
 *     - Creating tasks through the companion object
 *     - Verifying correct percentage calculation
 *     - Testing with different progress states (0%, 30%, 50%, 100%)
 *     - Checking custom progress bar lengths
 *     - Ensuring all progress bar components are displayed correctly
 *
 */
class VisualizableTaskTest {

    @Test
    fun `companion object invoke should create VisualizableTask with properties`() {
        val task = VisualizableTask("Test Task", 5, 10)

        assertEquals("Test Task", task.name)
        assertEquals(5, task.current)
        assertEquals(10, task.total)
    }

    @Test
    fun `createProgressBar should show correct percentage`() {
        val task = VisualizableTask("Task", 5, 10)
        val progressBar = task.createProgressBar()

        assertTrue(progressBar.contains("50%"), "Progress bar should show 50%: $progressBar")
    }

    @Test
    fun `createProgressBar should show correct completion count`() {
        val task = VisualizableTask("Task", 5, 10)
        val progressBar = task.createProgressBar()

        assertTrue(progressBar.contains("(5/10)"), "Progress bar should show (5/10): $progressBar")
    }

    @Test
    fun `createProgressBar should include task name`() {
        val taskName = "Test Task"
        val task = VisualizableTask(taskName, 5, 10)
        val progressBar = task.createProgressBar()

        assertTrue(progressBar.startsWith(taskName), "Progress bar should start with task name: $progressBar")
    }

    @Test
    fun `createProgressBar should respect custom length`() {
        val task = VisualizableTask("Task", 5, 10)
        val customLength = 20
        val progressBar = task.createProgressBar(customLength)

        // Count the characters between the brackets
        val bracketContent = progressBar.substringAfter('[').substringBefore(']')
        assertEquals(customLength, bracketContent.length, "Progress bar should have specified length: $progressBar")
    }

    @Test
    fun `createProgressBar should show empty bar for zero progress`() {
        val task = VisualizableTask("Task", 0, 10)
        val progressBar = task.createProgressBar(10)

        // The first character in the bar should be '>' and the rest spaces
        val barContent = progressBar.substringAfter('[').substringBefore(']')
        assertEquals(">         ", barContent, "Progress bar should show empty bar: $progressBar")
        assertTrue(progressBar.contains("0%"), "Progress bar should show 0%: $progressBar")
    }

    @Test
    fun `createProgressBar should show full bar for complete task`() {
        val task = VisualizableTask("Task", 10, 10)
        val progressBar = task.createProgressBar(10)

        // All characters in the bar should be '='
        val barContent = progressBar.substringAfter('[').substringBefore(']')
        assertEquals("==========", barContent, "Progress bar should show full bar: $progressBar")
        assertTrue(progressBar.contains("100%"), "Progress bar should show 100%: $progressBar")
    }

    @Test
    fun `createProgressBar should handle partial completion correctly`() {
        val task = VisualizableTask("Task", 3, 10)
        val progressBar = task.createProgressBar(10)

        // 3/10 = 30%, so 3 characters should be '=', one should be '>', and the rest spaces
        val barContent = progressBar.substringAfter('[').substringBefore(']')
        assertEquals("===>      ", barContent, "Progress bar should show 30% completion: $progressBar")
        assertTrue(progressBar.contains("30%"), "Progress bar should show 30%: $progressBar")
    }
}
