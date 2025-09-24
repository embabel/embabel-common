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
package com.embabel.common.core.config

import java.net.URL
import java.util.*

/**
 * Core domain models for property representation and analysis
 */

/**
 * Represents a property file discovered in the classpath with hierarchy information.
 *
 * This data class encapsulates all metadata about a property source including its
 * location, hierarchy depth for precedence calculation, and loaded properties.
 */
data class PropertySourceInfo(
    val url: URL,
    val fileName: String,
    val depth: Int,
    val jarPath: String,
    val properties: Properties
) {
    companion object {
        /**
         * Load property source info from a URL with automatic hierarchy calculation.
         *
         * @param url The URL pointing to the property file
         * @param fileName The name of the property file
         * @return PropertySourceInfo with loaded properties and calculated hierarchy depth
         */
        fun load(url: URL, fileName: String): PropertySourceInfo {
            val properties = url.openStream().use { stream ->
                Properties().apply { load(stream) }
            }

            return PropertySourceInfo(
                url = url,
                fileName = fileName,
                depth = calculateDepth(url),
                jarPath = extractJarPath(url),
                properties = properties
            )
        }

        /**
         * Calculate the hierarchy depth of a JAR/resource URL.
         *
         * Depth calculation logic:
         * - Nested JARs: Each nesting level (!) adds 10 points
         * - Path depth: Each directory separator (/) adds 1 point
         * - Higher depth = higher precedence in merging
         *
         * URL Examples:
         * - `jar:file:/app.jar!/config.properties` - file inside a JAR
         * - `jar:file:/app.jar!/BOOT-INF/lib/core.jar!/config.properties` - file inside a JAR that's inside another JAR
         * - `file:/user/app/config.properties` - regular file system file
         *
         * Depth Examples:
         * - `jar:file:/app.jar!/config.properties` = depth 10 (1 nesting × 10 + 0 path depth)
         * - `jar:file:/app.jar!/BOOT-INF/lib/core.jar!/config.properties` = depth 20 (2 nesting × 10 + 0 path depth)
         * - `file:/user/app/lib/module/config.properties` = depth 5 (0 nesting + 5 slashes in path)
         */
        private fun calculateDepth(url: URL): Int {
            val urlPath = url.toString()

            return when {
                // Nested JAR scenario: /app.jar!/BOOT-INF/lib/module.jar!/config.properties
                urlPath.contains("!/") -> {
                    val nestingLevel = urlPath.count { it == '!' }
                    val pathDepth = urlPath.substringAfterLast("!/").count { it == '/' }
                    nestingLevel * 10 + pathDepth // Nesting is weighted more heavily
                }

                // Regular file system: /user/app/lib/module.jar
                // File system resources should have highest precedence (application level)
                urlPath.startsWith("file:") -> {
                    val pathDepth = urlPath.substringAfter("file:").count { it == '/' }
                    1000 + pathDepth // File system gets highest precedence
                }

                // Other protocols - treat as shallow
                else -> 0
            }
        }

        /**
         * Extract the JAR path for logging and debugging purposes.
         */
        private fun extractJarPath(url: URL): String {
            val urlString = url.toString()
            return when {
                urlString.startsWith("jar:file:") -> {
                    urlString.substringAfter("jar:file:").substringBefore("!")
                }
                urlString.startsWith("file:") -> {
                    urlString.substringAfter("file:")
                }
                else -> urlString
            }
        }
    }
}

/**
 * Result of property analysis showing all sources, conflicts, and merge details.
 *
 * Used for debugging and understanding property loading behavior across JARs.
 */
data class PropertyAnalysis(
    val fileName: String,
    val sources: List<PropertySourceInfo>,
    val mergedProperties: Properties,
    /** Map of property key names to their conflict details */
    val conflicts: Map<String, List<PropertyConflict>>
) {
    /** Number of property sources discovered */
    val sourceCount: Int get() = sources.size

    /** Total number of properties in final merged result */
    val propertyCount: Int get() = mergedProperties.size

    /** Number of property keys that had conflicts during merging */
    val conflictCount: Int get() = conflicts.size

    /** Whether any conflicts were detected during merging */
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()
}

/**
 * Represents a property conflict during hierarchical merging.
 *
 * Shows which sources contributed values for the same property key
 * and which value was ultimately selected based on hierarchy precedence.
 */
data class PropertyConflict(
    val key: String,
    val value: String,
    val source: PropertySourceInfo,
    val overridden: Boolean
) {
    /** Whether this value won the conflict resolution */
    val isWinner: Boolean get() = !overridden
}
