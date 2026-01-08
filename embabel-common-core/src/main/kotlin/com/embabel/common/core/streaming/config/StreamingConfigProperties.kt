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
package com.embabel.common.core.streaming.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for streaming operations.
 * Controls buffering, timeouts, retry behavior, and error handling for reactive streams.
 *
 * Can be externalized via application properties using the prefix: embabel.platform.streaming
 *
 * Example configuration:
 * ```yaml
 * embabel:
 *   platform:
 *     streaming:
 *       buffer:
 *         maxSize: 1000
 *         duration: 100ms
 *       timeout:
 *         streamTimeout: 5m
 *       retry:
 *         maxAttempts: 3
 *       errorHandling:
 *         continueOnLineError: true
 * ```
 */
@ConfigurationProperties(prefix = "embabel.platform.streaming")
data class StreamingConfigProperties(

    /**
     * Buffer configuration for stream processing
     */
    val buffer: BufferConfig = BufferConfig(),

    /**
     * Timeout configuration for stream operations
     */
    val timeout: TimeoutConfig = TimeoutConfig(),

    /**
     * Retry configuration for failed stream operations
     */
    val retry: RetryConfig = RetryConfig(),

    /**
     * Error handling configuration
     */
    val errorHandling: ErrorHandlingConfig = ErrorHandlingConfig()

) {

    /**
     * Buffer configuration for reactive streams
     */
    data class BufferConfig(
        /**
         * Maximum number of objects to buffer in memory per stream
         */
        val maxSize: Int = 1000,

        /**
         * Time-based buffering duration - emit buffered items after this duration
         */
        val duration: Duration = Duration.ofMillis(100),

        /**
         * Maximum number of objects to collect before emitting a batch
         */
        val batchSize: Int = 10,

        /**
         * Backpressure buffer size - maximum items to buffer when downstream is slow
         */
        val backpressureSize: Int = 10000
    )

    /**
     * Timeout configuration for stream operations
     */
    data class TimeoutConfig(
        /**
         * Maximum time to wait for the entire stream to complete
         */
        val streamTimeout: Duration = Duration.ofMinutes(5),

        /**
         * Maximum time to wait for each individual line to be processed
         */
        val lineProcessingTimeout: Duration = Duration.ofSeconds(10)
    )

    /**
     * Retry configuration for failed operations
     */
    data class RetryConfig(
        /**
         * Maximum number of retry attempts for the entire stream
         */
        val maxAttempts: Int = 3,

        /**
         * Maximum number of retry attempts for individual line parsing
         */
        val maxLineRetries: Int = 2,

        /**
         * Base delay between retry attempts (exponential backoff)
         */
        val baseDelay: Duration = Duration.ofMillis(100),

        /**
         * Maximum delay between retry attempts
         */
        val maxDelay: Duration = Duration.ofSeconds(2)
    )

    /**
     * Error handling configuration
     */
    data class ErrorHandlingConfig(
        /**
         * Whether to continue processing after individual line failures
         */
        val continueOnLineError: Boolean = true,

        /**
         * Whether to log failed lines at WARN level instead of ERROR
         */
        val logFailuresAsWarning: Boolean = false,

        /**
         * Maximum number of consecutive line failures before terminating stream
         */
        val maxConsecutiveFailures: Int = 10,

        /**
         * Whether to include original line content in error messages (may expose sensitive data)
         */
        val includeLineInErrors: Boolean = false
    )
}
