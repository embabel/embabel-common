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
package com.embabel.common.core.types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @see [PaginatedDataRequest]
 *     - Tests the offset() and limit() methods
 *     - Verifies correct calculation of offset based on page and pageSize
 *     - Verifies limit() returns pageSize
 *     - Tests edge cases like first page and different page sizes
 */
class PaginatedDataRequestTest {

    @Test
    fun `offset should return zero for first page`() {
        // Create a simple implementation of PaginatedDataRequest for the first page
        val request = createRequest(page = 1, pageSize = 10)

        // Verify offset is 0 for the first page
        assertEquals(0, request.offset())
    }

    @Test
    fun `offset should calculate correctly for pages beyond first`() {
        // Test with different page numbers
        val request1 = createRequest(page = 2, pageSize = 10)
        val request2 = createRequest(page = 3, pageSize = 10)
        val request3 = createRequest(page = 10, pageSize = 10)

        // Verify offset calculations
        assertEquals(10, request1.offset())
        assertEquals(20, request2.offset())
        assertEquals(90, request3.offset())
    }

    @Test
    fun `offset should calculate correctly with different page sizes`() {
        // Test with different page sizes
        val request1 = createRequest(page = 2, pageSize = 5)
        val request2 = createRequest(page = 2, pageSize = 15)
        val request3 = createRequest(page = 2, pageSize = 100)

        // Verify offset calculations
        assertEquals(5, request1.offset())
        assertEquals(15, request2.offset())
        assertEquals(100, request3.offset())
    }

    @Test
    fun `limit should return pageSize`() {
        // Test with different page sizes
        val request1 = createRequest(page = 1, pageSize = 10)
        val request2 = createRequest(page = 1, pageSize = 20)
        val request3 = createRequest(page = 2, pageSize = 30)

        // Verify limit returns pageSize
        assertEquals(10, request1.limit())
        assertEquals(20, request2.limit())
        assertEquals(30, request3.limit())
    }

    @Test
    fun `offset should handle edge case with large page numbers`() {
        // Test with a large page number
        val request = createRequest(page = 1000, pageSize = 50)

        // Verify offset calculation
        assertEquals(49950, request.offset())
    }

    @Test
    fun `offset should handle edge case with small page size`() {
        // Test with a small page size
        val request1 = createRequest(page = 10, pageSize = 1)
        val request2 = createRequest(page = 100, pageSize = 1)

        // Verify offset calculation
        assertEquals(9, request1.offset())
        assertEquals(99, request2.offset())
    }

    // Helper method to create a simple implementation of PaginatedDataRequest
    private fun createRequest(page: Int, pageSize: Int): PaginatedDataRequest {
        return object : PaginatedDataRequest {
            override val page: Int = page
            override val pageSize: Int = pageSize
        }
    }
}
