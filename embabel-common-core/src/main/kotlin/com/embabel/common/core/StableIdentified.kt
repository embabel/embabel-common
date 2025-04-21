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
package com.embabel.common.core

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Implemented by objects that have a stable identifier,
 * regardless of whether they're persisted.
 * For example, the id is set before possible persistence.
 */
interface StableIdentified : Identified {

    @get:Schema(
        description = "object id",
        example = "aa0fd6de-2318-41e3-9dd4-104c6e02392f",
        required = true,
    )
    override val id: String
}
