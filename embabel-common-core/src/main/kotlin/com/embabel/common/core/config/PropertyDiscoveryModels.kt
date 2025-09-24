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

/**
 * Discovery and configuration models for cross-JAR property loading
 */

/**
 * Sealed class representing property discovery results.
 *
 * Provides type-safe handling of different discovery outcomes:
 * - Success: Sources found and loaded
 * - Empty: No sources found (not necessarily an error)
 * - Error: Discovery failed due to exception
 */
sealed class DiscoveryResult {

    /**
     * Successful discovery with property sources found.
     */
    data class Success(val sources: List<PropertySourceInfo>) : DiscoveryResult() {
        val sourceCount: Int get() = sources.size
        val isEmpty: Boolean get() = sources.isEmpty()
    }

    /**
     * No property sources found for the given filename.
     * This is not necessarily an error condition.
     */
    data class Empty(val fileName: String) : DiscoveryResult()

    /**
     * Discovery failed due to an exception.
     */
    data class Error(val fileName: String, val cause: Throwable) : DiscoveryResult()
}

/**
 * Configuration options for cross-JAR property loading behavior.
 *
 * Controls various aspects of the discovery and merging process:
 * - Logging verbosity
 * - Error handling strategy
 * - Performance optimizations
 */
data class CrossJarConfig(
    /**
     * Whether to log property conflicts during merging.
     * Useful for debugging configuration issues.
     */
    val logConflicts: Boolean = true,

    /**
     * Whether to log detailed merge process information.
     * Shows source discovery and hierarchy precedence decisions.
     */
    val logMergeProcess: Boolean = true,

    /**
     * Whether to fail fast on discovery or loading errors.
     * If false, returns empty properties and logs errors.
     */
    val failOnError: Boolean = false,

    /**
     * Whether to cache merged property results.
     * Improves performance but may not reflect runtime changes.
     */
    val cacheResults: Boolean = true
) {
    companion object {
        /**
         * Default configuration for production use.
         */
        val DEFAULT = CrossJarConfig()

        /**
         * Quiet configuration with minimal logging.
         */
        val QUIET = CrossJarConfig(
            logConflicts = false,
            logMergeProcess = false
        )

        /**
         * Debug configuration with verbose logging and no caching.
         */
        val DEBUG = CrossJarConfig(
            logConflicts = true,
            logMergeProcess = true,
            cacheResults = false
        )

        /**
         * Strict configuration that fails on any errors.
         */
        val STRICT = CrossJarConfig(
            failOnError = true
        )
    }
}
