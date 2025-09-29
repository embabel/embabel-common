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

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order


/**
 * Loads Cross-JAR properties.
 * Main use case: cross libraries properties aggregation.
 * Properties overload logic in general case is not deterministic, known limitation of JDK ClassUtils::getResources
 * @PropertySourcesee since Spring Core Framework 6.1 supports classpath* -syntax:
 * <href>https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/PropertySource.html</href>
 */
@Configuration
@PropertySource("classpath*:embabel-platform.properties")
@PropertySource("classpath*:embabel-application.properties")
@Order(Ordered.HIGHEST_PRECEDENCE)
class CommonPlatformPropertiesLoader {

    private val logger = LoggerFactory.getLogger(CommonPlatformPropertiesLoader::class.java)

    @PostConstruct
    fun init() {
        logger.info("Common properties loaded from classpath:embabel-platform.properties and embabel-application.properties")
    }

}
