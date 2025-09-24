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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import java.util.*

/**
 * Spring Configuration for Cross-JAR Properties Loading
 *
 * This configuration automatically discovers and merges `embabel-platform.properties`
 * and `embabel-application.properties` files from across all JARs in the classpath
 * with smart hierarchical precedence.
 *
 * ## Usage
 *
 * Import this configuration in your Spring application:
 *
 * ```kotlin
 * @Configuration
 * @Import(CrossJarPropertiesConfiguration::class)
 * class MyApplicationConfiguration {
 *     // Your other configuration
 * }
 * ```
 *
 * After importing, all merged properties are automatically available for `@Value` injection:
 *
 * ```kotlin
 * @Service
 * class MyService {
 *     @Value("\${embabel.platform.name:default}")
 *     private lateinit var platformName: String
 *
 *     @Value("\${embabel.application.environment:dev}")
 *     private lateinit var environment: String
 * }
 * ```
 *
 * ## Property Files Loaded
 *
 * - `embabel-platform.properties` - Platform-level configuration
 * - `embabel-application.properties` - Application-level configuration
 *
 * Both files are discovered across ALL JARs and merged with smart hierarchy precedence.
 *
 * ## Integration Notes
 *
 * - **Pure Spring Framework**: No Spring Boot dependencies required
 * - **High Precedence**: Loaded early with `HIGHEST_PRECEDENCE` order
 * - **Override Friendly**: System properties and environment variables can still override
 * - **Graceful Degradation**: Missing files don't break application startup
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
class CrossJarPropertiesConfiguration {

    companion object {
        private val logger = LoggerFactory.getLogger(CrossJarPropertiesConfiguration::class.java)

        /**
         * Creates a PropertySourcesPlaceholderConfigurer with merged cross-JAR properties.
         *
         * This bean is marked as @Primary to ensure it takes precedence over any default
         * Spring property configuration, while still allowing system properties and
         * environment variables to override the loaded values.
         *
         * Note: This method is static to avoid Spring container lifecycle issues with
         * BeanFactoryPostProcessor beans as recommended by Spring documentation.
         *
         * @return Configured PropertySourcesPlaceholderConfigurer with merged properties
         */
        @Bean
        @Primary
        @JvmStatic
        fun crossJarPropertySourcesConfigurer(): PropertySourcesPlaceholderConfigurer {
            logger.info("Initializing Cross-JAR Property Sources")

            return PropertySourcesPlaceholderConfigurer().apply {
                try {
                    // Load platform and application properties with smart hierarchical merging
                    val platformProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-platform.properties")
                    val applicationProps = CrossJarPropertiesUtil.loadCrossJarProperties("embabel-application.properties")

                    // Combine properties with application properties taking precedence over platform
                    val mergedProps = Properties().apply {
                        putAll(platformProps)
                        putAll(applicationProps) // Application properties override platform properties
                    }

                    setProperties(mergedProps)
                    setLocalOverride(true) // Allow system properties and environment variables to override

                    logger.info("Cross-JAR properties configured successfully:\n  Platform properties: ${platformProps.size}\n  Application properties: ${applicationProps.size}\n  Total merged properties: ${mergedProps.size}")

                } catch (e: Exception) {
                    logger.error("Failed to configure cross-JAR properties - using empty configuration", e)
                    setProperties(Properties()) // Graceful degradation
                }
            }
        }
    }

    /**
     * Configure CrossJarPropertiesUtil with default settings.
     *
     * This method is called during Spring context initialization to set up
     * the utility with appropriate logging and caching configuration.
     */
    @Bean
    fun crossJarPropertiesUtilConfigurer(): CrossJarConfigurer {
        return CrossJarConfigurer().also {
            CrossJarPropertiesUtil.configure(CrossJarConfig.DEFAULT)
            logger.debug("CrossJarPropertiesUtil configured with default settings")
        }
    }
}

/**
 * Simple marker class to ensure CrossJarPropertiesUtil configuration happens.
 *
 * This class exists solely to trigger the configuration of CrossJarPropertiesUtil
 * during Spring context initialization through the factory method above.
 */
class CrossJarConfigurer
