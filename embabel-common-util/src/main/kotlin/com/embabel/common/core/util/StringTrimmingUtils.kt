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
package com.embabel.common.core.util

const val DEFAULT_ELLIPSIS: String = "..."

/**
 * Trim a string to a maximum length, keeping the rightmost keepRight characters. Use
 * default ellipsis
 * @param s string to trim
 * @param max max length to return
 * @param keepRight number of characters to keep from the right
 * @return trimmed string
 */
fun trim(s: String?, max: Int, keepRight: Int, ellipsis: String = DEFAULT_ELLIPSIS): String? {
    require(max >= ellipsis.length + keepRight) { "max must be >= ellipsis.length() + keepRight" }
    if (s == null) {
        return null
    }
    if (s.length <= max) {
        return s
    }
    return s.substring(0, max - keepRight - ellipsis.length) + ellipsis + s.substring(s.length - keepRight)
}

fun removeWhitespace(s: String?): String? {
    return if (s == null) s else s.replace("\\s".toRegex(), "")
}
