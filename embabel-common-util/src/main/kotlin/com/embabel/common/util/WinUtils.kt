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

import com.sun.jna.platform.win32.Kernel32
import org.apache.commons.lang3.SystemUtils

/**
 * Utility class for Windows-specific operations.
 */
@ExcludeFromJacocoGeneratedReport(reason = "Windows-specific operations")
class WinUtils {

    companion object {

        const val UTF8_CODEPAGE = 65001

        /**
         * Returns true if the current OS is Windows.
         */
        @kotlin.jvm.JvmStatic
        fun IS_OS_WINDOWS(): Boolean {
            return SystemUtils.IS_OS_WINDOWS
        }

        /**
         * Sets the console code page to UTF-8.
         */
        @kotlin.jvm.JvmStatic
        fun CHCP_TO_UTF8() { //NOSONAR
            Kernel32.INSTANCE.SetConsoleCP(UTF8_CODEPAGE)
            Kernel32.INSTANCE.SetConsoleOutputCP(UTF8_CODEPAGE)
            println("Active Console Code Page: ${Kernel32.INSTANCE.GetConsoleCP()}")
        }

        /**
         * Returns the active console code page.
         */
        @kotlin.jvm.JvmStatic
        fun ACTIVE_CONSOLE_CODEPAGE(): Int { //NOSONAR
            return Kernel32.INSTANCE.GetConsoleCP()
        }

        /**
         * Returns true if the ASCII table is supported.
         */
        @kotlin.jvm.JvmStatic
        fun ASCII_TABLE_SUPPORTED(): Boolean {
            if (IS_OS_WINDOWS()) {
                return UTF8_CODEPAGE == ACTIVE_CONSOLE_CODEPAGE()
            }
            //if not windows then it is supported regardless of what the code page is
            return true
        }
    }
}
