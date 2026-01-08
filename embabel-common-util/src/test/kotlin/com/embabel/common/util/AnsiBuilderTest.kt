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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 *  @see [AnsiBuilderTest]: Tests formatting text with ANSI color codes including:
 *     - Converting hex colors to RGB
 *     - Building styles incrementally
 *     - String extension functions for color and style
 *     - Testing all the formatting functions
 */
class AnsiBuilderTest {

    private val esc = "\u001B"

    @Test
    fun `hexToRgb should convert hex color to RGB values`() {
        // Test with red color (0xFF0000)
        val red = hexToRgb(0xFF0000)
        assertEquals(Triple(255, 0, 0), red)

        // Test with green color (0x00FF00)
        val green = hexToRgb(0x00FF00)
        assertEquals(Triple(0, 255, 0), green)

        // Test with blue color (0x0000FF)
        val blue = hexToRgb(0x0000FF)
        assertEquals(Triple(0, 0, 255), blue)

        // Test with mixed color (0x4080C0)
        val mixed = hexToRgb(0x4080C0)
        assertEquals(Triple(64, 128, 192), mixed)
    }

    @Test
    fun `ansi function should format text with ANSI codes`() {
        val formatted = ansi("Hello", AnsiColor.RED)
        assertEquals("${esc}[31mHello${esc}[0m", formatted)

        // Test with multiple styles
        val multiStyle = ansi("Hello", AnsiColor.RED, AnsiStyle.BOLD)
        assertEquals("${esc}[31;1mHello${esc}[0m", multiStyle)
    }

    @Test
    fun `AnsiBuilder should build styles incrementally`() {
        val builder = AnsiBuilder()
            .withStyle(AnsiColor.BLUE)
            .withStyle(AnsiStyle.BOLD)

        val formatted = builder.format("Hello")
        assertEquals("${esc}[34;1mHello${esc}[0m", formatted)
    }

    @Test
    fun `AnsiBuilder should return unformatted text when no styles are applied`() {
        val builder = AnsiBuilder()
        val unformatted = builder.format("Hello")
        assertEquals("Hello", unformatted)
    }

    @Test
    fun `AnsiBuilder copy should create independent instance`() {
        val original = AnsiBuilder().withStyle(AnsiColor.RED)
        val copy = original.copy()

        // Modify the copy
        copy.withStyle(AnsiStyle.BOLD)

        // Original should only have the red color
        assertEquals("${esc}[31mTest${esc}[0m", original.format("Test"))

        // Copy should have both red color and bold
        assertEquals("${esc}[31;1mTest${esc}[0m", copy.format("Test"))
    }

    @Test
    fun `AnsiBuilder combine should join formatted strings`() {
        val part1 = ansi("Hello", AnsiColor.RED)
        val part2 = ansi("World", AnsiColor.BLUE)

        val combined = AnsiBuilder.combine(part1, " ", part2)
        assertEquals("${esc}[31mHello${esc}[0m ${esc}[34mWorld${esc}[0m", combined)
    }

    @Test
    fun `String extension functions should apply correct formatting`() {
        // Test color extension function
        assertEquals("${esc}[31mTest${esc}[0m", "Test".color(AnsiColor.RED))

        // Test RGB color function with Triple
        val rgb = Triple(100, 150, 200)
        assertEquals("${esc}[38;2;100;150;200mTest${esc}[0m", "Test".color(rgb))

        // Test RGB color function with hex integer
        assertEquals("${esc}[38;2;255;0;0mTest${esc}[0m", "Test".color(0xFF0000))

        // Test style extension functions
        assertEquals("${esc}[1mTest${esc}[0m", "Test".bold())
        assertEquals("${esc}[3mTest${esc}[0m", "Test".italic())
        assertEquals("${esc}[4mTest${esc}[0m", "Test".underline())
        assertEquals("${esc}[41mTest${esc}[0m", "Test".bgColor(AnsiBgColor.BG_RED))
    }

    @Test
    fun `concatFormatted should join strings correctly`() {
        val part1 = ansi("Red", AnsiColor.RED)
        val part2 = ansi("Blue", AnsiColor.BLUE)

        val result = concatFormatted(part1, " and ", part2)
        assertEquals("${esc}[31mRed${esc}[0m and ${esc}[34mBlue${esc}[0m", result)
    }

    @Test
    fun `styled extension function should apply multiple styles`() {
        val styled = "Test".styled {
            withStyle(AnsiColor.GREEN).withStyle(AnsiStyle.BOLD)
        }

        assertEquals("${esc}[32;1mTest${esc}[0m", styled)
    }
}
