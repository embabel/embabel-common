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

import org.slf4j.LoggerFactory
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Cross-JAR Properties Loader with Smart Hierarchical Merging
 *
 * This utility solves the problem of loading and merging same-named property files
 * across multiple JARs in the classpath with intelligent precedence resolution.
 *
 * ## Core Problem Solved
 *
 * In multi-JAR applications, you often have the same configuration file name
 * (e.g., `embabel-platform.properties`) in multiple JARs:
 * - `user-app.jar!/embabel-platform.properties` (user overrides)
 * - `embabel-agent.jar!/embabel-platform.properties` (library defaults)
 * - `embabel-core.jar!/embabel-platform.properties` (core defaults)
 *
 * Standard Spring property loading only finds the FIRST occurrence and ignores
 * valuable configuration from other JARs. This utility discovers ALL instances
 * and merges them intelligently.
 *
 * ## Smart Hierarchical Merging
 *
 * The utility applies "deeper JAR hierarchy = higher precedence" logic:
 * - User application JARs override library JARs
 * - Specific modules override general modules
 * - Nested deployments (Spring Boot fat JARs) have higher precedence
 *
 * ## Spring Framework Integration
 *
 * Leverages proven Spring facilities:
 * - `PathMatchingResourcePatternResolver` for reliable `classpath*:` discovery
 * - Spring's resource loading and URL handling
 * - Integrates seamlessly with Spring's property resolution system
 *
 * ## Usage Examples
 *
 * ### Basic Usage
 * ```kotlin
 * // Load and merge all embabel-platform.properties files
 * val platformProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
 *
 * // Properties from deeper JARs automatically override shallower ones
 * val platformName = platformProps.getProperty("embabel.platform.name")
 * ```
 *
 * ### Spring Integration
 * ```kotlin
 * @Configuration
 * class MyConfiguration {
 *
 *     @Bean
 *     @Primary
 *     fun propertySourcesConfigurer(): PropertySourcesPlaceholderConfigurer {
 *         return PropertySourcesPlaceholderConfigurer().apply {
 *             // Load merged properties from all JARs
 *             val platformProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
 *             val appProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-application.properties")
 *
 *             // Combine and make available for @Value injection
 *             val allProps = Properties().apply {
 *                 putAll(platformProps)
 *                 putAll(appProps)
 *             }
 *
 *             setProperties(allProps)
 *             setLocalOverride(true) // Allow system properties to override
 *         }
 *     }
 * }
 *
 * @Service
 * class MyService {
 *     @Value("\${embabel.platform.name:default}")
 *     private lateinit var platformName: String // Automatically injected from merged properties
 * }
 * ```
 *
 * ### Debugging and Analysis
 * ```kotlin
 * // Analyze property loading for debugging
 * val analysis = CrossJarPropertiesUtil.analyzeCrossJarProperties("embabel-platform.properties")
 *
 * println("Found ${analysis.sourceCount} sources")
 * println("Final properties: ${analysis.propertyCount}")
 *
 * if (analysis.hasConflicts) {
 *     println("Conflicts detected:")
 *     analysis.conflicts.forEach { (key, conflicts) ->
 *         val winner = conflicts.first { it.isWinner }
 *         println("  $key: winner from depth ${winner.source.depth}")
 *     }
 * }
 * ```
 *
 * ### Configuration Options
 *
 * Choose the appropriate configuration for your environment:
 *
 * ```kotlin
 * // Production Environment (Recommended)
 * CrossJarPropertiesUtil.configure(CrossJarConfig.QUIET)
 * // - Minimal logging (clean production logs)
 * // - Graceful degradation (missing files don't crash app)
 * // - Caching enabled (optimal performance)
 *
 * // Development/Troubleshooting
 * CrossJarPropertiesUtil.configure(CrossJarConfig.DEBUG)
 * // - Verbose logging (shows discovery details and conflicts)
 * // - No caching (always fresh data for debugging)
 * // - Graceful error handling
 *
 * // Testing/CI Validation
 * CrossJarPropertiesUtil.configure(CrossJarConfig.STRICT)
 * // - Fail-fast on any errors (catches config problems early)
 * // - Full logging enabled
 * // - NOT recommended for production (crashes on missing files)
 *
 * // Custom Configuration
 * CrossJarPropertiesUtil.configure(CrossJarConfig(
 *     logConflicts = true,      // Log property conflicts
 *     logMergeProcess = false,  // Skip verbose merge logging
 *     failOnError = false,      // Graceful degradation
 *     cacheResults = true       // Enable performance caching
 * ))
 * ```
 *
 * ## Hierarchy Depth Calculation
 *
 * The smart precedence is based on calculated "depth" values:
 *
 * | URL Pattern | Depth | Example |
 * |-------------|-------|---------|
 * | `/app.jar!/config.properties` | 10 | Simple JAR nesting |
 * | `/app.jar!/BOOT-INF/lib/core.jar!/config.properties` | 20 | Spring Boot fat JAR |
 * | `/user/app/lib/module/config.properties` | 4 | File system depth |
 *
 * Higher depth values win conflicts, ensuring user configurations override defaults.
 *
 * ## Performance Considerations
 *
 * - **Caching**: Results are cached by default for performance
 * - **Lazy Loading**: Uses Spring's efficient resource discovery
 * - **Graceful Degradation**: Never fails the application startup
 *
 * ## Thread Safety
 *
 * This utility is thread-safe and can be used concurrently from multiple threads.
 * Internal caching uses `ConcurrentHashMap` for safe concurrent access.
 */
object CrossJarPropertiesUtil {

    private val logger = LoggerFactory.getLogger(CrossJarPropertiesUtil::class.java)
    private val cache = ConcurrentHashMap<String, Properties>()
    private val resolver = PathMatchingResourcePatternResolver()
    private var config = CrossJarConfig.DEFAULT

    /**
     * Load and merge all instances of a property file across the entire classpath
     * with hierarchical precedence (deeper JAR paths override shallower ones).
     *
     * This is the main entry point for cross-JAR property loading. It discovers
     * all instances of the specified file using Spring's `classpath*:` mechanism
     * and applies smart hierarchical merging.
     *
     * @param fileName The name of the property file to load (e.g., "embabel-platform.properties")
     * @return Merged Properties object with deeper sources taking precedence
     *
     * @see analyzeCrossJarProperties for detailed analysis and debugging
     */
    fun loadCrossJarProperties(fileName: String): Properties {
        return if (config.cacheResults) {
            cache.computeIfAbsent(fileName) { loadAndMergeProperties(it) }
        } else {
            loadAndMergeProperties(fileName)
        }
    }

    /**
     * Analyze property loading for debugging - shows all sources, conflicts, and merge details.
     *
     * This method provides comprehensive analysis of the property loading process,
     * including source discovery, conflict detection, and final merge results.
     * Useful for understanding configuration behavior and troubleshooting issues.
     *
     * @param fileName The name of the property file to analyze
     * @return PropertyAnalysis with detailed information about sources and conflicts
     */
    fun analyzeCrossJarProperties(fileName: String): PropertyAnalysis {
        val discoveryResult = discoverPropertySources(fileName)

        return when (discoveryResult) {
            is DiscoveryResult.Success -> {
                val conflicts = detectConflicts(discoveryResult.sources)
                val merged = mergeWithHierarchy(discoveryResult.sources)

                PropertyAnalysis(
                    fileName = fileName,
                    sources = discoveryResult.sources,
                    mergedProperties = merged,
                    conflicts = conflicts
                ).also { analysis ->
                    if (config.logMergeProcess) logAnalysis(analysis)
                }
            }

            is DiscoveryResult.Empty -> {
                PropertyAnalysis(fileName, emptyList(), Properties(), emptyMap())
            }

            is DiscoveryResult.Error -> {
                if (config.failOnError) throw discoveryResult.cause
                PropertyAnalysis(fileName, emptyList(), Properties(), emptyMap())
            }
        }
    }

    /**
     * Configure the behavior of cross-JAR property loading.
     *
     * When caching is disabled (`cacheResults = false`), any existing cached data is automatically cleared to ensure:
     * - **Data irrelevance**: When you disable caching, any existing cached data becomes irrelevant
     * - **Fresh data**: Ensures immediate fresh loading when caching is turned off
     * - **Memory cleanup**: Frees cached properties when they won't be used
     *
     * @param newConfig Configuration options for logging, caching, and error handling
     */
    fun configure(newConfig: CrossJarConfig) {
        config = newConfig
        if (!newConfig.cacheResults) {
            clearCache()
        }
        logger.debug("CrossJarPropertiesUtil configured: $newConfig")
    }

    /**
     * Clear the property cache - useful for testing or dynamic reloading.
     *
     * Forces fresh loading of properties on next access. Use with caution
     * in production as it may impact performance.
     */
    fun clearCache() {
        cache.clear()
        logger.debug("Cross-JAR properties cache cleared")
    }

    // Internal Implementation

    private fun loadAndMergeProperties(fileName: String): Properties {
        logger.debug("Loading cross-JAR properties for: $fileName")

        return try {
            when (val discoveryResult = discoverPropertySources(fileName)) {
                is DiscoveryResult.Success -> {
                    when {
                        discoveryResult.isEmpty -> {
                            logger.warn("No '$fileName' files found in classpath")
                            if (config.failOnError) {
                                throw IllegalStateException("No property files found for '$fileName' in classpath (STRICT mode)")
                            }
                            Properties()
                        }

                        discoveryResult.sourceCount == 1 -> {
                            discoveryResult.sources.first().properties.also { properties ->
                                if (config.logMergeProcess) {
                                    logger.info("Loaded '$fileName' from 1 source: ${properties.size} properties")

                                    // Log actual key/value pairs at INFO level for transparency
                                    val propertiesString = properties.entries.joinToString("\n") { (key, value) -> "  $key=$value" }
                                    logger.info("Properties for '$fileName':\n$propertiesString")
                                }
                            }
                        }

                        else -> {
                            mergeWithHierarchy(discoveryResult.sources).also { merged ->
                                if (config.logMergeProcess) {
                                    logger.info("Merged '$fileName' from ${discoveryResult.sourceCount} sources: ${merged.size} final properties")

                                    // Log actual key/value pairs at INFO level for transparency
                                    val propertiesString = merged.entries.joinToString("\n") { (key, value) -> "  $key=$value" }
                                    logger.info("Final merged properties for '$fileName':\n$propertiesString")
                                }
                            }
                        }
                    }
                }

                is DiscoveryResult.Empty -> {
                    logger.warn("No '$fileName' files found in classpath")
                    if (config.failOnError) {
                        throw IllegalStateException("No property files found for '$fileName' in classpath (STRICT mode)")
                    }
                    Properties()
                }

                is DiscoveryResult.Error -> {
                    logger.error("Failed to discover property sources for '$fileName'", discoveryResult.cause)
                    if (config.failOnError) throw discoveryResult.cause
                    Properties()
                }
            }

        } catch (e: Exception) {
            logger.error("Failed to load cross-JAR properties for '$fileName': ${e.message}")
            if (config.failOnError) throw e
            Properties() // Graceful degradation
        }
    }

    private fun discoverPropertySources(fileName: String): DiscoveryResult {
        return try {
            // Leverage Spring's proven classpath* discovery mechanism
            val resources = resolver.getResources("classpath*:$fileName")
            val sources = resources.asSequence()
                .filter { resource ->
                    // Check if resource truly exists and is readable
                    resource.exists() && resource.isReadable
                }
                .mapNotNull { resource ->
                    try {
                        PropertySourceInfo.load(resource.url, fileName)
                    } catch (e: Exception) {
                        logger.warn("Failed to load properties from ${resource.url}: ${e.message}")
                        null // Skip this source but continue with others
                    }
                }
                .toList()

            // Log discovery results at INFO level for visibility
            if (sources.isEmpty()) {
                logger.info("No instances of '$fileName' found in classpath")
                DiscoveryResult.Empty(fileName)
            } else {
                val sourcesList = sources.joinToString("\n") { "  ${it.url} (${it.properties.size} properties)" }
                logger.info("Discovered ${sources.size} instances of '$fileName':\n$sourcesList")
                DiscoveryResult.Success(sources)
            }

        } catch (e: Exception) {
            logger.error("Failed to discover property sources for '$fileName'", e)
            DiscoveryResult.Error(fileName, e)
        }
    }

    private fun mergeWithHierarchy(sources: List<PropertySourceInfo>): Properties {
        if (sources.isEmpty()) return Properties()
        if (sources.size == 1) return sources.first().properties

        logger.debug("Starting hierarchical merge of ${sources.size} property sources")

        // Sort by depth: shallow first, deep last
        // But reverse discovery order so earlier discovered sources (dependents) override later ones (dependencies)
        val sortedSources = sources.sortedWith(compareBy<PropertySourceInfo> { it.depth }.thenByDescending { sources.indexOf(it) })

        return sortedSources.fold(Properties()) { merged, source ->
            if (config.logMergeProcess) {
                logger.debug("Merging depth ${source.depth}: ${source.jarPath} (${source.properties.size} properties)")
            }
            merged.apply {
                putAll(source.properties) // Later (deeper) sources override earlier (shallower) ones
            }
        }.also { result ->
            logger.debug("Hierarchical merge complete: ${result.size} final properties")
        }
    }

    private fun detectConflicts(sources: List<PropertySourceInfo>): Map<String, List<PropertyConflict>> {
        if (sources.size <= 1) return emptyMap()

        val allKeys = sources.flatMap { it.properties.keys.map { key -> key as String } }.toSet()
        val conflicts = mutableMapOf<String, MutableList<PropertyConflict>>()

        allKeys.forEach { key ->
            val valuesForKey = sources.mapNotNull { source ->
                source.properties.getProperty(key)?.let { value ->
                    PropertyConflict(key, value, source, false)
                }
            }

            if (valuesForKey.size > 1) {
                // Mark all but the highest depth as overridden
                val sortedByDepth = valuesForKey.sortedBy { it.source.depth }
                val conflictList = sortedByDepth.mapIndexed { index, conflict ->
                    conflict.copy(overridden = index < sortedByDepth.size - 1)
                }
                conflicts[key] = conflictList.toMutableList()
            }
        }

        return conflicts
    }

    private fun logAnalysis(analysis: PropertyAnalysis) {
        if (!logger.isInfoEnabled) return

        logger.info("=== Cross-JAR Property Analysis: ${analysis.fileName} ===")
        logger.info("Discovered ${analysis.sourceCount} sources:")

        analysis.sources.sortedBy { it.depth }.forEach { source ->
            logger.info("  Depth ${source.depth}: ${source.jarPath} (${source.properties.size} properties)")
        }

        if (analysis.hasConflicts && config.logConflicts) {
            logger.info("Property conflicts found (${analysis.conflictCount}):")
            analysis.conflicts.forEach { (key, conflicts) ->
                val winner = conflicts.first { it.isWinner }
                logger.info("  '$key': ${conflicts.size} sources, winner: '${winner.value}' from depth ${winner.source.depth}")

                if (logger.isDebugEnabled) {
                    conflicts.forEach { conflict ->
                        val status = if (conflict.overridden) "OVERRIDDEN" else "WINNER"
                        logger.debug("    $status: '${conflict.value}' from ${conflict.source.jarPath}")
                    }
                }
            }
        }

        logger.info("Final result: ${analysis.propertyCount} properties")
        logger.info("=== End Analysis ===")
    }
}

/**
 * Kotlin extension for PathMatchingResourcePatternResolver integration
 */
fun PathMatchingResourcePatternResolver.loadCrossJarProperties(fileName: String): Properties =
    CrossJarPropertiesUtil.loadCrossJarProperties(fileName)
