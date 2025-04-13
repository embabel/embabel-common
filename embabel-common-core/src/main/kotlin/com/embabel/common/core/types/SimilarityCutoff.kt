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
 * Core criteria for similarity searching
 */
interface SimilarityCutoff {
    @get:Schema(
        description = "Threshold for similarity search. 0 means include all results",
        example = "0.8",
        minimum = "0",
        maximum = "1",
        required = true,
    )
    val similarityThreshold: Double

    @get:Schema(
        description = "Number of results to include",
        example = "5",
        minimum = "1",
        maximum = "100",
        required = true,
    )
    val topK: Int
}
