/*
* Copyright 2025 Embabel Software, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.embabel.common.util

data class VisualizedTask(
    val name: String,
    val current: Int,
    val total: Int,
)


/**
 * Create a progress bar as a string
 */
fun createProgressBar(task: VisualizedTask, length: Int = 50): String {
    val (name, current, total) = task
    val percent = (current * 100.0 / total).toInt()
    val completed = (length * current / total)

    return buildString {
        append("$name - [")
        repeat(length) { i ->
            append(
                when {
                    i < completed -> "="
                    i == completed -> ">"
                    else -> " "
                }
            )
        }
        append("] ")
        append("%3d%%".format(percent))
        append(" (%d/%d)".format(current, total))
    }
}
