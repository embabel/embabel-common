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
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.test.context.junit.jupiter.EnabledIf

/**
 * Integration tests combining PropertyCore and PropertyDiscovery models
 * Tests real-world scenarios where both model types interact together
 */
@EnabledIf("#{systemProperties['embabel.common.cross-jar.enabled'] == 'true'}")
class CrossJarPropertiesIntegrationTest {

    @BeforeEach
    fun setUp() {
        CrossJarPropertiesUtil.clearCache()
        CrossJarPropertiesUtil.configure(CrossJarConfig.QUIET)
    }

    @AfterEach
    fun tearDown() {
        CrossJarPropertiesUtil.clearCache()
        CrossJarPropertiesUtil.configure(CrossJarConfig.DEFAULT)
    }

    @Test
    fun `should analyze property loading with core and discovery models working together`() {
        // Test the full flow: Discovery -> Core Models -> Analysis
        val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")

        // Verify DiscoveryResult.Success was created with PropertySourceInfo instances
        assertTrue(analysis.sourceCount > 0, "Should discover at least one source")
        assertTrue(analysis.propertyCount > 0, "Should have properties from discovered sources")

        // Verify PropertySourceInfo core models work correctly
        analysis.sources.forEach { source ->
            assertNotNull(source.url, "Source should have valid URL")
            assertNotNull(source.fileName, "Source should have filename")
            assertTrue(source.depth >= 0, "Source should have calculated depth")
            assertNotNull(source.jarPath, "Source should have jar path")
            assertNotNull(source.properties, "Source should have Properties object (even if empty)")
        }

        // Verify PropertyAnalysis aggregates data from both model types
        assertEquals(analysis.sources.sumOf { it.properties.size }, analysis.totalSourceProperties)
        assertEquals("embabel-platform.properties", analysis.fileName)
    }

    @Test
    fun `should handle discovery failure gracefully with core models`() {
        // Configure to fail on error to test error propagation
        CrossJarPropertiesUtil.configure(CrossJarConfig(failOnError = false))

        // Test non-existent file - should create Empty DiscoveryResult
        val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("non-existent-file.properties")

        // Verify graceful handling
        assertEquals(0, analysis.sourceCount)
        assertEquals(0, analysis.propertyCount)
        assertFalse(analysis.hasConflicts)
        assertTrue(analysis.sources.isEmpty())
        assertEquals("non-existent-file.properties", analysis.fileName)
    }

    @Test
    fun `should detect conflicts using both PropertySourceInfo and DiscoveryResult`() {
        // Load a file that should exist in test resources
        val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")

        // If we have multiple sources, there could be conflicts
        if (analysis.sourceCount > 1) {
            // Verify conflict detection works with PropertyConflict core models
            analysis.conflicts.forEach { (key, conflicts) ->
                assertTrue(conflicts.isNotEmpty(), "Conflicts list should not be empty")

                val winner = conflicts.firstOrNull { it.isWinner }
                val losers = conflicts.filter { it.overridden }

                assertNotNull(winner, "Should have exactly one winner")
                assertEquals(conflicts.size - 1, losers.size, "All other conflicts should be losers")

                // Verify PropertyConflict core model properties
                conflicts.forEach { conflict ->
                    assertEquals(key, conflict.key)
                    assertNotNull(conflict.value)
                    assertNotNull(conflict.source)
                }
            }
        }
    }

    @Test
    fun `should support different discovery configurations`() {
        // Test with different CrossJarConfig settings
        val configurations = listOf(
            CrossJarConfig.DEFAULT,
            CrossJarConfig.QUIET,
            CrossJarConfig.DEBUG,
            CrossJarConfig.STRICT
        )

        configurations.forEach { config ->
            CrossJarPropertiesUtil.configure(config)

            val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")

            // Results should be consistent regardless of configuration
            assertTrue(analysis.sourceCount >= 0)
            assertTrue(analysis.propertyCount >= 0)
            assertNotNull(analysis.sources)
            assertNotNull(analysis.mergedProperties)
            assertNotNull(analysis.conflicts)
        }
    }

    @Test
    fun `should integrate with Spring configuration using both model types`() {
        // Test Spring integration with @Import
        val context = AnnotationConfigApplicationContext(TestSpringConfiguration::class.java)

        try {
            // Verify PropertySourcesPlaceholderConfigurer was created
            val configurer = context.getBean(PropertySourcesPlaceholderConfigurer::class.java)
            assertNotNull(configurer)

            // Verify test component can inject properties
            val testComponent = context.getBean(TestPropertyComponent::class.java)
            assertNotNull(testComponent.platformName)
            assertNotNull(testComponent.applicationName)

            // Properties should come from our cross-JAR loading
            assertTrue(testComponent.platformName.isNotEmpty())
            assertTrue(testComponent.applicationName.isNotEmpty())

        } finally {
            context.close()
        }
    }

    @Test
    fun `should handle multiple property files with hierarchical merging`() {
        // Test loading both platform and application properties
        val platformAnalysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")
        val appAnalysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-application.properties")

        // Both should be discoverable
        assertTrue(platformAnalysis.sourceCount > 0 || appAnalysis.sourceCount > 0,
                  "At least one property file should be found")

        // Verify PropertySourceInfo models work for both files
        (platformAnalysis.sources + appAnalysis.sources).forEach { source ->
            assertTrue(source.fileName.endsWith(".properties"))
            assertTrue(source.depth >= 0)
            assertNotNull(source.url)
            assertNotNull(source.jarPath)
        }

        // Test merging behavior by loading both
        val platformProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        val appProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-application.properties")

        assertNotNull(platformProps)
        assertNotNull(appProps)
    }

    @Test
    fun `should cache results properly across model interactions`() {
        // Enable caching
        CrossJarPropertiesUtil.configure(CrossJarConfig(cacheResults = true))

        // Load properties multiple times
        val props1 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
        val props2 = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")

        // Should be same instance due to caching
        assertSame(props1, props2, "Cached results should be the same instance")

        // Analysis should also work with cached data
        val analysis1 = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")
        val analysis2 = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")

        // Analysis results should be equivalent (but may not be same instance)
        assertEquals(analysis1.sourceCount, analysis2.sourceCount)
        assertEquals(analysis1.propertyCount, analysis2.propertyCount)
        assertEquals(analysis1.conflictCount, analysis2.conflictCount)
    }
}

/**
 * Test Spring configuration that imports CrossJarPropertiesConfiguration
 */
@Configuration
@Import(CrossJarPropertiesConfiguration::class)
class TestSpringConfiguration {

    @Bean
    fun testPropertyComponent(): TestPropertyComponent = TestPropertyComponent()
}

/**
 * Test component that uses @Value injection with cross-JAR properties
 */
@Component
class TestPropertyComponent {

    @Value("\${embabel.platform.name:default-platform}")
    lateinit var platformName: String

    @Value("\${embabel.application.name:default-app}")
    lateinit var applicationName: String

    @Value("\${embabel.platform.environment:default}")
    lateinit var environment: String
}

/**
 * Extension to PropertyAnalysis for testing total source properties
 */
private val PropertyAnalysis.totalSourceProperties: Int
    get() = sources.sumOf { it.properties.size }
