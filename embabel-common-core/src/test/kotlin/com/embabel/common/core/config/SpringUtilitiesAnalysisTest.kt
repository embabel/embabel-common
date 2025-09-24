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

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.core.io.support.ResourcePatternResolver
import java.util.*

/**
 * Analysis tests for Spring utilities used in Cross-JAR Properties Loading.
 *
 * This test class validates the behavior of Spring Framework utilities that underpin
 * our Cross-JAR Properties Loader implementation:
 *
 * - **PathMatchingResourcePatternResolver**: Tests `classpath*:` discovery behavior
 * - **PropertiesLoaderUtils**: Tests Spring's built-in property loading capabilities
 * - **URL hierarchy analysis**: Validates depth calculation for different resource types
 *
 * These tests help understand Spring's behavior and validate our assumptions about
 * how Spring discovers and loads resources across multiple JARs.
 *
 * ## Purpose
 *
 * 1. **Validate Spring utilities**: Ensure Spring's `classpath*:` discovery works as expected
 * 2. **Document behavior**: Show how Spring handles multiple property files
 * 3. **Debug resource discovery**: Help troubleshoot classpath resource issues
 * 4. **Test hierarchy calculation**: Validate our depth calculation algorithm
 *
 * ## Test Categories
 *
 * - **Resource Discovery**: Tests for finding property files across JARs
 * - **Property Loading**: Tests for loading and merging properties
 * - **Hierarchy Analysis**: Tests for calculating JAR/file hierarchy depth
 *
 * @see PathMatchingResourcePatternResolver
 * @see PropertiesLoaderUtils
 * @see CrossJarPropertiesUtil
 */
class SpringUtilitiesAnalysisTest {

    private val logger = LoggerFactory.getLogger(SpringUtilitiesAnalysisTest::class.java)

    @Test
    fun `test ResourcePatternResolver with wildcard classpath`() {
        logger.info("=== Testing ResourcePatternResolver with classpath* ===")

        val resolver: ResourcePatternResolver = PathMatchingResourcePatternResolver()

        // Test discovering all embabel-platform.properties files
        val resources = resolver.getResources("classpath*:embabel-platform.properties")

        logger.info("Found ${resources.size} resources with classpath*")

        val totalProps = resources.filter { it.exists() }.sumOf { resource ->
            try {
                val props = Properties()
                resource.inputStream.use { props.load(it) }
                props.size
            } catch (e: Exception) {
                logger.info("Error loading ${resource.url}: ${e.message}")
                0
            }
        }

        logger.info("Total properties across all resources: $totalProps")
    }

    @Test
    fun `test PropertiesLoaderUtils loadAllProperties`() {
        logger.info("=== Testing PropertiesLoaderUtils.loadAllProperties ===")

        try {
            val mergedProps = PropertiesLoaderUtils.loadAllProperties("embabel-platform.properties")

            logger.info("PropertiesLoaderUtils result: ${mergedProps.size} properties")
            if (mergedProps.isNotEmpty()) {
                val sample = mergedProps.entries.take(3)
                logger.info("Sample properties: ${sample.joinToString { "${it.key}=${it.value}" }}")
            }

        } catch (e: Exception) {
            logger.info("PropertiesLoaderUtils failed: ${e.message}")
        }
    }

    @Test
    fun `test multiple property files analysis`() {
        logger.info("=== Testing Multiple Property Files ===")

        val fileNames = listOf(
            "embabel-platform.properties",
            "embabel-application.properties",
            "application.properties",
            "nonexistent.properties"
        )

        val results = fileNames.map { fileName ->
            val resolver = PathMatchingResourcePatternResolver()
            val resources = resolver.getResources("classpath*:$fileName")
            val propsCount = try {
                PropertiesLoaderUtils.loadAllProperties(fileName).size
            } catch (e: Exception) {
                0
            }
            "$fileName: ${resources.size} resources, $propsCount properties"
        }

        logger.info("Analysis results:\n${results.joinToString("\n")}")
    }

    @Test
    fun `test hierarchy depth analysis`() {
        logger.info("=== Testing URL Hierarchy Analysis ===")

        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath*:*.properties")

        val depthAnalysis = resources.take(10).map { resource ->
            val url = resource.url.toString()
            val depth = calculateTestDepth(url)
            "Depth $depth: ${url.substringAfterLast('/')}"
        }

        logger.info("Hierarchy analysis (first 10):\n${depthAnalysis.joinToString("\n")}")
    }

    private fun calculateTestDepth(url: String): Int {
        return when {
            url.contains("!/") -> {
                val nestingLevel = url.count { it == '!' }
                val pathDepth = url.substringAfterLast("!/").count { it == '/' }
                nestingLevel * 10 + pathDepth
            }
            url.startsWith("file:") -> {
                url.substringAfter("file:").count { it == '/' }
            }
            else -> 0
        }
    }
}
