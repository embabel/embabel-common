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

import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource

/**
 * Pull a random message from a file
 */
class RandomFromFileMessageGenerator(
    val url: String,
) : MessageList {

    private val messageFile: Resource = DefaultResourceLoader().getResource(url)

    override val messages: List<String>
        get() {
            val reader = messageFile.inputStream.bufferedReader()
            val result = mutableListOf<String>()
            var currentMultilineMessage: StringBuilder? = null

            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmedLine = line.trim()

                    when {
                        // Skip blank lines and comments when not in multiline mode
                        currentMultilineMessage == null && (trimmedLine.isBlank() || trimmedLine.startsWith("#")) -> {
                            // Skip this line
                        }

                        // Start of multiline message
                        currentMultilineMessage == null && trimmedLine.startsWith("\"\"\"") -> {
                            // If the line contains an end delimiter too, handle as single line case
                            if (trimmedLine.endsWith("\"\"\"") && trimmedLine.length > 6) {
                                // Extract content between quotes and add as a message
                                val content = trimmedLine.substring(3, trimmedLine.length - 3)
                                result.add(content)
                            } else {
                                // Start collecting multiline content
                                currentMultilineMessage = StringBuilder()
                                // Add first line content if there's anything after the opening delimiter
                                if (trimmedLine.length > 3) {
                                    currentMultilineMessage?.append(trimmedLine.substring(3))
                                }
                            }
                        }

                        // End of multiline message
                        currentMultilineMessage != null && trimmedLine.endsWith("\"\"\"") -> {
                            // Add the last line without the closing delimiter
                            if (trimmedLine.length > 3) {
                                currentMultilineMessage?.append("\n")
                                currentMultilineMessage?.append(trimmedLine.substring(0, trimmedLine.length - 3))
                            }

                            // Add the completed multiline message to results
                            result.add(currentMultilineMessage.toString())
                            currentMultilineMessage = null
                        }

                        // Continue collecting multiline message content
                        currentMultilineMessage != null -> {
                            currentMultilineMessage?.append("\n")
                            currentMultilineMessage?.append(line) // Use original line to preserve leading whitespace
                        }

                        // Normal single-line message
                        else -> {
                            result.add(trimmedLine)
                        }
                    }
                }
            }

            // Handle unclosed multiline message (optional based on requirements)
            currentMultilineMessage?.let {
                result.add(it.toString())
            }

            return result
        }
}
