/*
* Copyright 2025 Embabel Software, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.embabel.common.util

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

}
