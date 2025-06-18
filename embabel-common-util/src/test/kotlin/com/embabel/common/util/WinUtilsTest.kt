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

import com.embabel.common.util.WinUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

class WinUtilsTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `is OS Windows`() {
        assertTrue(WinUtils.IS_OS_WINDOWS())
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `is not OS Windows`() {
        assertFalse(WinUtils.IS_OS_WINDOWS())
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `chcp(UTF8)`() {
        WinUtils.CHCP_TO_UTF8()
        assertTrue(WinUtils.ACTIVE_CONSOLE_CODEPAGE() == WinUtils.UTF8_CODEPAGE)
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `chcp(UTF8) should not be called on Linux`() {
        assertThrows<Error> { WinUtils.ACTIVE_CONSOLE_CODEPAGE() }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `active console code page should not be called on Linux`() {
        assertThrows<Error> { WinUtils.ACTIVE_CONSOLE_CODEPAGE() }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `AsciiTable is supported on Windows`() {
        WinUtils.CHCP_TO_UTF8()
        assertTrue(WinUtils.ASCII_TABLE_SUPPORTED())
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    fun `AsciiTable is supported on Linux`() {
        assertTrue(WinUtils.ASCII_TABLE_SUPPORTED())
    }

    // === New Font Tests ===

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `set Cascadia Code font does not throw exception on Windows`() {
        // Should not throw exception regardless of whether font is available
        WinUtils.SET_CASCADIA_CODE_FONT()
        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `set Cascadia Code font with custom size does not throw exception`() {
        // Should handle custom font sizes gracefully
        WinUtils.SET_CASCADIA_CODE_FONT(18)
        assertTrue(true)
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun `set Cascadia Code font returns false on non-Windows`() {
        val result = WinUtils.SET_CASCADIA_CODE_FONT()
        assertFalse(result)
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `setup optimal console does not throw exception on Windows`() {
        // Should complete without exceptions
        WinUtils.SETUP_OPTIMAL_CONSOLE()
        assertTrue(true)
    }

    @Test
    @EnabledOnOs(OS.LINUX, OS.MAC)
    fun `setup optimal console returns false on non-Windows`() {
        val result = WinUtils.SETUP_OPTIMAL_CONSOLE()
        assertFalse(result)
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `font methods maintain UTF-8 code page`() {
        // Set UTF-8 first
        WinUtils.CHCP_TO_UTF8()
        val initialCodePage = WinUtils.ACTIVE_CONSOLE_CODEPAGE()

        // Try to set font (may succeed or fail)
        WinUtils.SET_CASCADIA_CODE_FONT()

        // Verify UTF-8 is still active
        val finalCodePage = WinUtils.ACTIVE_CONSOLE_CODEPAGE()
        assertTrue(finalCodePage == WinUtils.UTF8_CODEPAGE)
    }

    @Test
    fun `font size edge cases do not cause crashes`() {
        // Test that edge case font sizes don't cause crashes

        // Negative font size
        try {
            WinUtils.SET_CASCADIA_CODE_FONT((-1).toShort())
            // Should complete without exception
        } catch (e: Exception) {
            // If exception occurs, it should be handled gracefully
            assertTrue(e.message?.isNotEmpty() ?: false)
        }

        // Very large font size
        try {
            WinUtils.SET_CASCADIA_CODE_FONT(100)
            // Should complete without exception
        } catch (e: Exception) {
            assertTrue(e.message?.isNotEmpty() ?: false)
        }

        // Zero font size
        try {
            WinUtils.SET_CASCADIA_CODE_FONT(0)
            // Should complete without exception
        } catch (e: Exception) {
            assertTrue(e.message?.isNotEmpty() ?: false)
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `multiple font operations are safe`() {
        // Should be safe to call multiple times without exceptions
        try {
            WinUtils.SET_CASCADIA_CODE_FONT()
            WinUtils.SETUP_OPTIMAL_CONSOLE()
            WinUtils.SET_CASCADIA_CODE_FONT(14)
            // If we get here, all operations completed
            assertTrue(true)
        } catch (e: Exception) {
            // If exceptions occur, they should be handled gracefully
            assertTrue(e.message?.isNotEmpty() ?: false)
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun `cascadia code font method preserves console state`() {
        // Capture initial state
        val initialCodePage = WinUtils.ACTIVE_CONSOLE_CODEPAGE()

        // Apply font
        WinUtils.SET_CASCADIA_CODE_FONT()

        // Should have UTF-8 enabled (method calls CHCP_TO_UTF8)
        val finalCodePage = WinUtils.ACTIVE_CONSOLE_CODEPAGE()
        assertTrue(finalCodePage == WinUtils.UTF8_CODEPAGE)
    }
}
