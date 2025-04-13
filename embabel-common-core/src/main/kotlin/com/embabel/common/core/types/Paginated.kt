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

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Standard interface for paginated data requests and results.
 */
interface Paginated {

    @get:Schema(
        description = "The current page number, from 1",
        example = "1",
        required = true,
    )
    val page: Int

    @get:Schema(
        description = "The number of items per page",
        example = "10",
        required = true,
    )
    val pageSize: Int

}

interface PaginatedDataRequest : Paginated {

    fun offset(): Int {
        return (page - 1) * pageSize
    }

    fun limit(): Int {
        return pageSize
    }
}

interface PaginatedDataResult : Paginated {

    @get:Schema(
        description = "The total number of results",
        example = "100",
        required = true,
    )
    val total: Int

    fun nextPage(): Int {
        return page + 1
    }

    fun previousPage(): Int {
        return page - 1
    }

    fun totalPages(): Int {
        return total / pageSize
    }

    fun hasNext(): Boolean {
        return page < totalPages()
    }

    fun hasPrevious(): Boolean {
        return page > 1
    }

}
