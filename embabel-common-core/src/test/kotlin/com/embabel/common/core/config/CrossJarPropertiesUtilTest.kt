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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


/**
 * Unit tests for CrossJarPropertiesUtil core functionality
 */
class CrossJarPropertiesUtilTest {

    @BeforeEach
    fun setUp() {
        CrossJarPropertiesUtil.clearCache()
        CrossJarPropertiesUtil.configure(CrossJarConfig.QUIET) // Reduce noise in tests
    }

    @AfterEach
    fun tearDown() {
        CrossJarPropertiesUtil.clearCache()
        CrossJarPropertiesUtil.configure(CrossJarConfig.DEFAULT)
    }

    @Test
    fun `should load properties from existing file`() {
        // Test with our known test file
        val properties = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")

        assertNotNull(properties)
        assertTrue(properties.size > 0)
        assertEquals("embabel-platform", properties.getProperty("embabel.platform.name"))
        assertEquals("embabel-common-core", properties.getProperty("embabel.platform.core.module"))
    }

    @Test
    fun `should return empty properties for non-existent file`() {
        val properties = CrossJarPropertiesUtil.loadCrossJarProperties("non-existent.properties")

        assertNotNull(properties)
        assertEquals(0, properties.size)
    }

    @Test
    fun `should cache properties by default`() {
        val config = CrossJarConfig(cacheResults = true)
        CrossJarPropertiesUtil.configure(config)

        // Load twice
        val properties1 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        val properties2 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")

        // Should be the same instance due to caching
        assertSame(properties1, properties2)
    }

    @Test
    fun `should not cache when caching disabled`() {
        val config = CrossJarConfig(cacheResults = false)
        CrossJarPropertiesUtil.configure(config)

        // Load twice
        val properties1 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        val properties2 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")

        // Should be different instances when caching disabled
        assertNotSame(properties1, properties2)
        // But content should be the same
        assertEquals(properties1.getProperty("embabel.platform.name"),
                    properties2.getProperty("embabel.platform.name"))
    }

    @Test
    fun `should analyze properties with detailed information`() {
        val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")

        assertNotNull(analysis)
        assertEquals("embabel-platform.properties", analysis.fileName)
        assertTrue(analysis.sourceCount > 0)
        assertTrue(analysis.propertyCount > 0)
        assertNotNull(analysis.mergedProperties)
        assertNotNull(analysis.sources)
    }

    @Test
    fun `should handle empty analysis for non-existent file`() {
        val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("non-existent.properties")

        assertNotNull(analysis)
        assertEquals("non-existent.properties", analysis.fileName)
        assertEquals(0, analysis.sourceCount)
        assertEquals(0, analysis.propertyCount)
        assertFalse(analysis.hasConflicts)
    }

    @Test
    fun `should fail fast when configured in strict mode with missing file`() {
        CrossJarPropertiesUtil.configure(CrossJarConfig.STRICT)

        // This should throw for non-existent files in strict mode
        assertThrows<Exception> {
            CrossJarPropertiesUtil.loadCrossJarProperties("non-existent-file.properties")
        }
    }

    @Test
    fun `should clear cache correctly and force fresh loading`() {
        // Enable caching to test cache behavior
        CrossJarPropertiesUtil.configure(CrossJarConfig(cacheResults = true))

        // First load - should populate cache
        val properties1 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")

        // Second load - should return same cached instance
        val properties2 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        assertSame(properties1, properties2, "Should return cached instance before clear")

        // Clear cache
        CrossJarPropertiesUtil.clearCache()

        // Third load - should be fresh instance (not from cache)
        val properties3 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        assertNotSame(properties1, properties3, "Should return fresh instance after cache clear")

        // Content should still be the same
        assertEquals(properties1.getProperty("embabel.platform.name"),
                    properties3.getProperty("embabel.platform.name"),
                    "Content should be identical even after cache clear")
    }

    @Test
    fun `should apply different configurations correctly`() {
        /**
         * Test different CrossJarConfig presets:
         *
         * - **DEBUG**: Verbose logging enabled, caching disabled (for development)
         *   - logConflicts=true, logMergeProcess=true, cacheResults=false
         *   - Useful for troubleshooting property loading issues
         *
         * - **QUIET**: Minimal logging, caching enabled (for production)
         *   - logConflicts=false, logMergeProcess=false, cacheResults=true
         *   - Reduces log noise in production environments
         *
         * - **STRICT**: Fail-fast on errors, logging enabled (for validation)
         *   - failOnError=true, ensures any issues cause immediate failure
         *   - Useful for catching configuration problems early
         */

        // Test DEBUG config - verbose logging, no caching
        CrossJarPropertiesUtil.configure(CrossJarConfig.DEBUG)
        val debugProperties = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        assertNotNull(debugProperties)

        // Test QUIET config - minimal logging, caching enabled
        CrossJarPropertiesUtil.configure(CrossJarConfig.QUIET)
        val quietProperties = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        assertNotNull(quietProperties)

        // Content should be the same regardless of configuration
        assertEquals(debugProperties.getProperty("embabel.platform.name"),
                    quietProperties.getProperty("embabel.platform.name"),
                    "Property content should be identical across different configurations")
    }

    @Test
    fun `should load both platform and application properties`() {
        val platformProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        val appProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-application.properties")

        assertNotNull(platformProps)
        assertNotNull(appProps)

        // Check specific properties exist
        assertNotNull(platformProps.getProperty("embabel.platform.name"))
        assertNotNull(appProps.getProperty("embabel.application.name"))

        // They should be different property sets
        assertNotEquals(platformProps.getProperty("embabel.platform.name"),
                       appProps.getProperty("embabel.application.name"))
    }
}
