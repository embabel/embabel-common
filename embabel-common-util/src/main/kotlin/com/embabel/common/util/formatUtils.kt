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

/**
 * Indents the string by the specified number of levels.
 * @param level The number of levels to indent. This is a relative number.
 * @param tabStr The string to use for each level of indentation
 * @return The indented string
 */
fun String.indent(
  level: Int = 1,
  tabStr: String = "  ",
): String = "${tabStr.repeat(level)}$this"

/**
 * Indents each line of the string by the specified number of levels.
 * @param level The number of levels to indent. This is a relative number.
 * @param removeBlankLines If true, blank lines are removed
 * @param tabStr The string to use for each level of indentation
 * @return The indented string
 */
fun String.indentLines(
  level: Int = 1,
  removeBlankLines: Boolean = true,
  tabStr: String = "  ",
): String =
  this
    .lines()
    .run { if (removeBlankLines) filter{it.isNotBlank()} else this }
    .joinToString("\n") { it.indent(level, tabStr) }
