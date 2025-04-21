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
 * Interface representing a task.
 * Implementations can be visualized as a progress bar.
 */
interface VisualizableTask {

    /**
     * Name of the task
     */
    val name: String

    /**
     * Current step of the task, out of total steps
     */
    val current: Int

    /**
     * Total steps in the task
     */
    val total: Int

    /**
     * Create a progress bar as a string
     */
    fun createProgressBar(length: Int = 50): String {
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

    companion object {
        operator fun invoke(
            name: String,
            current: Int,
            total: Int
        ): VisualizableTask = object : VisualizableTask {
            override val name: String = name
            override val current: Int = current
            override val total: Int = total
        }
    }
}
