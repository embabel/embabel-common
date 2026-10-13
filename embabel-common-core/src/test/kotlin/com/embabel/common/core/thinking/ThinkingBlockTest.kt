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
package com.embabel.common.core.thinking

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ThinkingBlockTest {

    @Nested
    inner class Origin {

        @Test
        fun `has exactly two values`() {
            assertEquals(2, ThinkingOrigin.entries.size)
        }

        @Test
        fun `NATIVE and PROMPT_EXTRACTED are distinct`() {
            assertNotEquals(ThinkingOrigin.NATIVE, ThinkingOrigin.PROMPT_EXTRACTED)
        }
    }

    @Nested
    inner class PromptBasedThinking {

        @Test
        fun `three-arg construction defaults to PROMPT_EXTRACTED`() {
            val block = ThinkingBlock(
                content = "I need to think about this",
                tagType = ThinkingTagType.TAG,
                tagValue = "think",
            )
            assertEquals(ThinkingOrigin.PROMPT_EXTRACTED, block.origin)
            assertTrue(block.metadata.isEmpty())
        }

        @Test
        fun `NO_PREFIX block defaults to PROMPT_EXTRACTED`() {
            val block = ThinkingBlock(
                content = "some reasoning",
                tagType = ThinkingTagType.NO_PREFIX,
                tagValue = "",
            )
            assertEquals(ThinkingOrigin.PROMPT_EXTRACTED, block.origin)
        }
    }

    @Nested
    inner class NativeThinking {

        @Test
        fun `native block carries NATIVE origin`() {
            val block = ThinkingBlock(
                content = "I should consider the options carefully",
                tagType = ThinkingTagType.NO_PREFIX,
                tagValue = "",
                origin = ThinkingOrigin.NATIVE,
            )
            assertEquals(ThinkingOrigin.NATIVE, block.origin)
        }

        @Test
        fun `native block carries provider metadata`() {
            val block = ThinkingBlock(
                content = "reasoning text",
                tagType = ThinkingTagType.NO_PREFIX,
                tagValue = "",
                origin = ThinkingOrigin.NATIVE,
                metadata = mapOf("signature" to "sig-abc123"),
            )
            assertEquals("sig-abc123", block.metadata["signature"])
        }

        @Test
        fun `metadata is empty by default`() {
            val block = ThinkingBlock(
                content = "x",
                tagType = ThinkingTagType.NO_PREFIX,
                tagValue = "",
                origin = ThinkingOrigin.NATIVE,
            )
            assertTrue(block.metadata.isEmpty())
        }
    }
}
