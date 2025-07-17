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

import com.embabel.common.util.ConsoleFontInfoEx.Coord
import com.sun.jna.platform.win32.Kernel32
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

class UtilRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        // Register dynamic proxies
        hints.proxies().registerJdkProxy(Kernel32::class.java)
        hints.proxies().registerJdkProxy(Kernel32Extended::class.java)

        // Register types for reflection
        hints.reflection().registerType(ConsoleFontInfoEx::class.java) {
            it.withMembers(
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            )
        }

        hints.reflection().registerType(Coord::class.java) {
            it.withMembers(
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            )
        }

        hints.reflection().registerType(Kernel32Extended::class.java) {
            it.withMembers(MemberCategory.INVOKE_DECLARED_METHODS)
        }
    }
}
