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
 * @param skipIndentFirstLine If true, the first line is not indented
 * @param tabStr The string to use for each level of indentation
 * @return The indented string
 */
fun String.indentLines(
  level: Int = 1,
  removeBlankLines: Boolean = true,
  skipIndentFirstLine: Boolean = false,
  tabStr: String = "  ",
): String =
  this
    .lines()
    .run { if (removeBlankLines) filter { it.isNotBlank() } else this }
    .mapIndexed { i, s -> i to s }
    .joinToString("\n") {
      if (skipIndentFirstLine && it.first == 0)
        it.second
      else
        it.second.indent(level, tabStr)
    }
