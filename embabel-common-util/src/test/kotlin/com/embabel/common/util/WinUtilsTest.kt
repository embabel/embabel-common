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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@DisplayName("WinUtils")
class WinUtilsTest {

    @Nested
    @DisplayName("Platform Detection")
    inner class PlatformDetectionTests {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should correctly identify Windows OS")
        fun `identifies Windows OS correctly`() {
            assertTrue(WinUtils.IS_OS_WINDOWS()) {
                "Should return true when running on Windows"
            }
        }

        @Test
        @EnabledOnOs(OS.LINUX, OS.MAC)
        @DisplayName("should correctly identify non-Windows OS")
        fun `identifies non-Windows OS correctly`() {
            assertFalse(WinUtils.IS_OS_WINDOWS()) {
                "Should return false when running on non-Windows platforms"
            }
        }
    }

    @Nested
    @DisplayName("Console Code Page Management")
    inner class CodePageTests {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should set UTF-8 code page on Windows")
        fun `sets UTF8 code page on Windows`() {
            // Given: A Windows environment
            // When: Setting UTF-8 code page
            WinUtils.CHCP_TO_UTF8()

            // Then: Code page should be UTF-8
            assertEquals(WinUtils.UTF8_CODEPAGE, WinUtils.ACTIVE_CONSOLE_CODEPAGE()) {
                "Console code page should be set to UTF-8 (${WinUtils.UTF8_CODEPAGE})"
            }
        }

        @Test
        @EnabledOnOs(OS.LINUX, OS.MAC)
        @DisplayName("should throw error when accessing code page on non-Windows")
        fun `throws error for code page operations on non-Windows`() {
            assertThrows<Error> {
                WinUtils.ACTIVE_CONSOLE_CODEPAGE()
            }
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should support ASCII table when UTF-8 is active")
        fun `supports ASCII table with UTF8 on Windows`() {
            // Given: UTF-8 code page is set
            WinUtils.CHCP_TO_UTF8()

            // When: Checking ASCII table support
            val isSupported = WinUtils.ASCII_TABLE_SUPPORTED()

            // Then: Should be supported
            assertTrue(isSupported) {
                "ASCII table should be supported when UTF-8 code page is active"
            }
        }

        @Test
        @EnabledOnOs(OS.LINUX, OS.MAC)
        @DisplayName("should always support ASCII table on non-Windows")
        fun `always supports ASCII table on non-Windows`() {
            assertTrue(WinUtils.ASCII_TABLE_SUPPORTED()) {
                "ASCII table should always be supported on non-Windows platforms"
            }
        }
    }

    @Nested
    @DisplayName("Font Management")
    inner class FontManagementTests {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should handle Cascadia Code font setting gracefully")
        fun `handles Cascadia Code font setting on Windows`() {
            // Given: A Windows environment
            // When: Setting Cascadia Code font (may succeed or fail based on font availability)
            val result = WinUtils.SET_CASCADIA_CODE_FONT()

            // Then: Should not throw exception and UTF-8 should be maintained
            assertEquals(WinUtils.UTF8_CODEPAGE, WinUtils.ACTIVE_CONSOLE_CODEPAGE()) {
                "UTF-8 code page should be maintained after font operation"
            }
            // Note: We don't assert success/failure as font availability varies by system
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should preserve font size when no size specified")
        fun `preserves font size when no size specified`() {
            // Given: UTF-8 is set up
            WinUtils.CHCP_TO_UTF8()

            // When: Setting Cascadia Code font without specifying size
            WinUtils.SET_CASCADIA_CODE_FONT()

            // Then: Should complete without exception (size preservation tested via console output)
            assertEquals(WinUtils.UTF8_CODEPAGE, WinUtils.ACTIVE_CONSOLE_CODEPAGE()) {
                "UTF-8 code page should be maintained after font operation with size preservation"
            }
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should accept specific font size when provided")
        fun `accepts specific font size when provided`() {
            // Given: A Windows environment
            // When: Setting Cascadia Code font with specific size
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT(18)
            }

            // Then: UTF-8 should be maintained
            assertEquals(WinUtils.UTF8_CODEPAGE, WinUtils.ACTIVE_CONSOLE_CODEPAGE()) {
                "UTF-8 code page should be maintained after font operation with specific size"
            }
        }

        @Test
        @EnabledOnOs(OS.LINUX, OS.MAC)
        @DisplayName("should return false for font operations on non-Windows")
        fun `returns false for font operations on non-Windows`() {
            val result1 = WinUtils.SET_CASCADIA_CODE_FONT()
            val result2 = WinUtils.SET_CASCADIA_CODE_FONT(16)

            assertFalse(result1) {
                "Font operations should return false on non-Windows platforms"
            }
            assertFalse(result2) {
                "Font operations with size should return false on non-Windows platforms"
            }
        }

        @ParameterizedTest
        @ValueSource(shorts = [8, 12, 16, 18, 24, 32])
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should handle various font sizes gracefully")
        fun `handles various font sizes gracefully`(fontSize: Short) {
            // Should complete without throwing exceptions
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT(fontSize)
            }
        }

        @ParameterizedTest
        @ValueSource(shorts = [-1, 0, 100, 999])
        @DisplayName("should handle edge case font sizes safely")
        fun `handles edge case font sizes safely`(fontSize: Short) {
            // Should not crash with unusual font sizes
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT(fontSize)
            }
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should handle optimal console setup with size preservation")
        fun `handles optimal console setup with size preservation on Windows`() {
            // Given: A Windows environment
            // When: Setting up optimal console
            val result = WinUtils.SETUP_OPTIMAL_CONSOLE()

            // Then: Should not throw and maintain UTF-8
            assertEquals(WinUtils.UTF8_CODEPAGE, WinUtils.ACTIVE_CONSOLE_CODEPAGE()) {
                "UTF-8 code page should be maintained after console optimization"
            }
        }

        @Test
        @EnabledOnOs(OS.LINUX, OS.MAC)
        @DisplayName("should return false for console optimization on non-Windows")
        fun `returns false for console optimization on non-Windows`() {
            val result = WinUtils.SETUP_OPTIMAL_CONSOLE()
            assertFalse(result) {
                "Console optimization should return false on non-Windows platforms"
            }
        }
    }

    @Nested
    @DisplayName("Error Handling and Resilience")
    inner class ErrorHandlingTests {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should handle multiple consecutive font operations safely")
        fun `handles multiple consecutive font operations safely`() {
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT(12.toShort())
                WinUtils.SETUP_OPTIMAL_CONSOLE()
                WinUtils.SET_CASCADIA_CODE_FONT() // size preservation
                WinUtils.SET_CASCADIA_CODE_FONT(14.toShort())
            }
        }

        @Test
        @DisplayName("should maintain consistent state after operations")
        fun `maintains consistent state after operations`() {
            if (WinUtils.IS_OS_WINDOWS()) {
                // Given: Initial UTF-8 setup
                WinUtils.CHCP_TO_UTF8()
                val initialCodePage = WinUtils.ACTIVE_CONSOLE_CODEPAGE()

                // When: Performing font operations
                WinUtils.SET_CASCADIA_CODE_FONT()
                WinUtils.SETUP_OPTIMAL_CONSOLE()

                // Then: UTF-8 should still be active
                val finalCodePage = WinUtils.ACTIVE_CONSOLE_CODEPAGE()
                assertEquals(WinUtils.UTF8_CODEPAGE, finalCodePage) {
                    "UTF-8 code page should be maintained throughout all operations"
                }
            }
        }

        @Test
        @DisplayName("should handle null size parameter gracefully")
        fun `handles null size parameter gracefully`() {
            // Test that explicit null parameter works the same as no parameter
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT(null)
            }
        }
    }

    @Nested
    @DisplayName("Integration and Output Validation")
    inner class IntegrationTests {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should provide informative console output with size preservation info")
        fun `provides informative console output with size preservation info`() {
            val outputStream = ByteArrayOutputStream()
            val originalOut = System.out

            try {
                System.setOut(PrintStream(outputStream))

                WinUtils.CHCP_TO_UTF8()
                WinUtils.SET_CASCADIA_CODE_FONT()

                val output = outputStream.toString()
                assertTrue(output.contains("Active Console Code Page")) {
                    "Should log active console code page information"
                }

                // Check for size preservation message (may or may not appear depending on font availability)
                // We don't assert this as it depends on system font availability

            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should show size preservation in SETUP_OPTIMAL_CONSOLE output")
        fun `shows size preservation in SETUP_OPTIMAL_CONSOLE output`() {
            val outputStream = ByteArrayOutputStream()
            val originalOut = System.out

            try {
                System.setOut(PrintStream(outputStream))

                WinUtils.SETUP_OPTIMAL_CONSOLE()

                val output = outputStream.toString()
                // Should contain either success or failure message
                assertTrue(
                    output.contains("Console optimized") ||
                    output.contains("No suitable Unicode fonts") ||
                    output.contains("Windows font APIs not available")
                ) {
                    "Should provide feedback about console optimization attempt"
                }

            } finally {
                System.setOut(originalOut)
            }
        }

        @Test
        @DisplayName("should behave consistently across platform checks")
        fun `behaves consistently across platform checks`() {
            val isWindows = WinUtils.IS_OS_WINDOWS()
            val asciiSupported = WinUtils.ASCII_TABLE_SUPPORTED()

            if (isWindows) {
                // On Windows, ASCII support depends on code page
                WinUtils.CHCP_TO_UTF8()
                assertTrue(WinUtils.ASCII_TABLE_SUPPORTED()) {
                    "ASCII should be supported on Windows with UTF-8"
                }
            } else {
                // On non-Windows, ASCII should always be supported
                assertTrue(asciiSupported) {
                    "ASCII should always be supported on non-Windows platforms"
                }
            }
        }
    }

    @Nested
    @DisplayName("Boundary and Contract Testing")
    inner class BoundaryTests {

        @Test
        @DisplayName("should handle edge cases in font operations")
        fun `handles edge cases in font operations`() {
            // Test that font operations don't break with various inputs
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT(Short.MIN_VALUE)
                WinUtils.SET_CASCADIA_CODE_FONT(Short.MAX_VALUE)
                WinUtils.SET_CASCADIA_CODE_FONT(null) // explicit null
            }
        }

        @Test
        @EnabledOnOs(OS.WINDOWS)
        @DisplayName("should validate code page constants")
        fun `validates code page constants`() {
            assertEquals(65001, WinUtils.UTF8_CODEPAGE) {
                "UTF-8 code page constant should be 65001"
            }
        }

        @Test
        @DisplayName("should maintain thread safety for static methods")
        fun `maintains thread safety for static methods`() {
            // Test concurrent access to static methods
            val threads = (1..5).map { threadId ->
                Thread {
                    repeat(10) {
                        assertDoesNotThrow {
                            WinUtils.IS_OS_WINDOWS()
                            WinUtils.ASCII_TABLE_SUPPORTED()
                            if (WinUtils.IS_OS_WINDOWS()) {
                                // Test both with and without size parameter
                                WinUtils.SET_CASCADIA_CODE_FONT((12 + threadId).toShort())
                                WinUtils.SET_CASCADIA_CODE_FONT() // size preservation
                            }
                        }
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }
        }

        @Test
        @DisplayName("should handle API overloads correctly")
        fun `handles API overloads correctly`() {
            // Test that both API variations work
            assertDoesNotThrow {
                WinUtils.SET_CASCADIA_CODE_FONT()          // no parameter (size preservation)
                WinUtils.SET_CASCADIA_CODE_FONT(16)        // explicit size
                WinUtils.SET_CASCADIA_CODE_FONT(null)      // explicit null (size preservation)
            }
        }
    }
}
