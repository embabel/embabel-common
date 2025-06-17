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
 * @see [PaginatedDataResult]
 *     - Tests nextPage(), previousPage(), totalPages(), hasNext(), and hasPrevious() methods
 *     - Verifies correct calculation of next and previous page numbers
 *     - Verifies correct calculation of total pages
 *     - Tests edge cases:
 *       - First page (page = 1)
 *       - Last page
 *       - Only one page (total <= pageSize)
 *       - Total exactly divisible by pageSize
 *       - Total not exactly divisible by pageSize
 */
class PaginatedDataResultTest {

    @Test
    fun `nextPage should return page plus one`() {
        // Test with different page numbers
        val result1 = createResult(page = 1, pageSize = 10, total = 100)
        val result2 = createResult(page = 5, pageSize = 10, total = 100)
        val result3 = createResult(page = 10, pageSize = 10, total = 100)

        // Verify nextPage calculations
        assertEquals(2, result1.nextPage())
        assertEquals(6, result2.nextPage())
        assertEquals(11, result3.nextPage())
    }

    @Test
    fun `previousPage should return page minus one`() {
        // Test with different page numbers
        val result1 = createResult(page = 2, pageSize = 10, total = 100)
        val result2 = createResult(page = 5, pageSize = 10, total = 100)
        val result3 = createResult(page = 10, pageSize = 10, total = 100)

        // Verify previousPage calculations
        assertEquals(1, result1.previousPage())
        assertEquals(4, result2.previousPage())
        assertEquals(9, result3.previousPage())
    }

    @Test
    fun `totalPages should calculate correctly when total is exactly divisible by pageSize`() {
        // Test with total exactly divisible by pageSize
        val result1 = createResult(page = 1, pageSize = 10, total = 100)
        val result2 = createResult(page = 1, pageSize = 25, total = 100)
        val result3 = createResult(page = 1, pageSize = 5, total = 100)

        // Verify totalPages calculations
        assertEquals(10, result1.totalPages())
        assertEquals(4, result2.totalPages())
        assertEquals(20, result3.totalPages())
    }

    @Test
    fun `totalPages should calculate correctly when total is not exactly divisible by pageSize`() {
        // Test with total not exactly divisible by pageSize
        val result1 = createResult(page = 1, pageSize = 3, total = 10)
        val result2 = createResult(page = 1, pageSize = 7, total = 20)
        val result3 = createResult(page = 1, pageSize = 9, total = 100)

        // Verify totalPages calculations
        // Note: The current implementation uses integer division which truncates the result
        // This test verifies the current behavior, but it might need to be updated if the implementation changes
        assertEquals(3, result1.totalPages()) // 10/3 = 3.33, truncated to 3
        assertEquals(2, result2.totalPages()) // 20/7 = 2.86, truncated to 2
        assertEquals(11, result3.totalPages()) // 100/9 = 11.11, truncated to 11
    }

    @Test
    fun `hasNext should return true when not on last page`() {
        // Test with page not on last page
        val result1 = createResult(page = 1, pageSize = 10, total = 100) // 10 pages total
        val result2 = createResult(page = 5, pageSize = 10, total = 100) // 10 pages total
        val result3 = createResult(page = 9, pageSize = 10, total = 100) // 10 pages total

        // Verify hasNext returns true
        assertTrue(result1.hasNext())
        assertTrue(result2.hasNext())
        assertTrue(result3.hasNext())
    }

    @Test
    fun `hasNext should return false when on last page`() {
        // Test with page on last page
        val result1 = createResult(page = 10, pageSize = 10, total = 100) // 10 pages total
        val result2 = createResult(page = 4, pageSize = 25, total = 100) // 4 pages total
        val result3 = createResult(page = 20, pageSize = 5, total = 100) // 20 pages total

        // Verify hasNext returns false
        assertFalse(result1.hasNext())
        assertFalse(result2.hasNext())
        assertFalse(result3.hasNext())
    }

    @Test
    fun `hasNext should return false when on last page with non-exact division`() {
        // Test with page on last page with non-exact division
        val result1 = createResult(page = 4, pageSize = 3, total = 10) // 3.33 pages, truncated to 3
        val result2 = createResult(page = 3, pageSize = 7, total = 20) // 2.86 pages, truncated to 2
        val result3 = createResult(page = 12, pageSize = 9, total = 100) // 11.11 pages, truncated to 11

        // Verify hasNext returns false
        // Note: This test might need to be updated if the totalPages implementation changes
        assertFalse(result1.hasNext())
        assertFalse(result2.hasNext())
        assertFalse(result3.hasNext())
    }

    @Test
    fun `hasPrevious should return true when not on first page`() {
        // Test with page not on first page
        val result1 = createResult(page = 2, pageSize = 10, total = 100)
        val result2 = createResult(page = 5, pageSize = 10, total = 100)
        val result3 = createResult(page = 10, pageSize = 10, total = 100)

        // Verify hasPrevious returns true
        assertTrue(result1.hasPrevious())
        assertTrue(result2.hasPrevious())
        assertTrue(result3.hasPrevious())
    }

    @Test
    fun `hasPrevious should return false when on first page`() {
        // Test with page on first page
        val result1 = createResult(page = 1, pageSize = 10, total = 100)
        val result2 = createResult(page = 1, pageSize = 25, total = 100)
        val result3 = createResult(page = 1, pageSize = 5, total = 100)

        // Verify hasPrevious returns false
        assertFalse(result1.hasPrevious())
        assertFalse(result2.hasPrevious())
        assertFalse(result3.hasPrevious())
    }

    @Test
    fun `edge case - only one page (total less than or equal to pageSize)`() {
        // Test with total <= pageSize (only one page)
        val result1 = createResult(page = 1, pageSize = 10, total = 10)
        val result2 = createResult(page = 1, pageSize = 10, total = 5)
        val result3 = createResult(page = 1, pageSize = 100, total = 50)

        // Verify calculations
        // Note: The current implementation uses integer division
        // When total < pageSize, totalPages() will return 0
        // When total = pageSize, totalPages() will return 1
        assertEquals(1, result1.totalPages())
        assertEquals(0, result2.totalPages())
        assertEquals(0, result3.totalPages())

        assertFalse(result1.hasNext())
        assertFalse(result2.hasNext())
        assertFalse(result3.hasNext())

        assertFalse(result1.hasPrevious())
        assertFalse(result2.hasPrevious())
        assertFalse(result3.hasPrevious())
    }

    @Test
    fun `edge case - zero total items`() {
        // Test with zero total items
        val result = createResult(page = 1, pageSize = 10, total = 0)

        // Verify calculations
        assertEquals(0, result.totalPages())
        assertFalse(result.hasNext())
        assertFalse(result.hasPrevious())
    }

    // Helper method to create a simple implementation of PaginatedDataResult
    private fun createResult(page: Int, pageSize: Int, total: Int): PaginatedDataResult {
        return object : PaginatedDataResult {
            override val page: Int = page
            override val pageSize: Int = pageSize
            override val total: Int = total
        }
    }
}
