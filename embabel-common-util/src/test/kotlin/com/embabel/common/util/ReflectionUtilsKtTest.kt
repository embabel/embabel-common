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

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private class NoSupertypes

private interface IThing

private open class CThing

private class InterfaceOnly : IThing

private class ClassOnly : CThing()

private class ClassAndInterface : IThing, CThing()

class ReflectionUtilsKtTest {

    @Nested
    inner class FindAllSupertypes {

        @Test
        fun `no supertypes`() {
            val result = findAllSupertypes(NoSupertypes::class.java)
            assertEquals(setOf(NoSupertypes::class.java), result)
        }

        @Test
        fun `find interface`() {
            val result = findAllSupertypes(InterfaceOnly::class.java)
            assertEquals(setOf(IThing::class.java, InterfaceOnly::class.java), result)
        }

        @Test
        fun `find class`() {
            val result = findAllSupertypes(ClassOnly::class.java)
            assertEquals(setOf(CThing::class.java, ClassOnly::class.java), result)
        }

        @Test
        fun `find both`() {
            val result = findAllSupertypes(ClassAndInterface::class.java)
            assertEquals(setOf(CThing::class.java, IThing::class.java, ClassAndInterface::class.java), result)
        }
    }

}
