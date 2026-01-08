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
package com.embabel.common.util

import org.slf4j.LoggerFactory
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.util.ClassUtils

/**
 * Find all supertypes of the given type.
 * Returns both classes and interfaces, including the class itself.
 */
fun findAllSupertypes(clazz: Class<*>): Set<Class<*>> {
    val supertypes = mutableSetOf<Class<*>>()
    supertypes += clazz.interfaces
    clazz.superclass?.let { superclass ->
        supertypes += clazz
        supertypes += findAllSupertypes(superclass)
    }
    return supertypes.toSet()
}


/**
 * Finds implementations of a given interface on the classpath using Spring utilities
 * @param interfaceClass The interface to find implementations for
 * @param basePackage The base package to scan (e.g. "com.yourcompany").
 * Defaults to be the first 2 parts of the interface name
 * @return List of classes that implement the given interface
 */
fun <T> findImplementationsOnClasspath(
    interfaceClass: Class<T>,
    basePackage: String? = null,
): List<Class<out T>> {
    require(interfaceClass.isInterface) { "${interfaceClass.simpleName} is not an interface" }

    val logger = LoggerFactory.getLogger("com.embabel.common.util.reflectionUtils")

    val resolver = PathMatchingResourcePatternResolver()
    val metadataReaderFactory = CachingMetadataReaderFactory(resolver)
    val implementations = mutableListOf<Class<out T>>()

    val packageToScan = basePackage ?: run {
        val packageParts = interfaceClass.name.split(".")
        if (packageParts.size >= 2) {
            // Take first 2 parts of the package
            "${packageParts[0]}.${packageParts[1]}"
        } else {
            // If the package doesn't have 2 parts, use the full package
            packageParts.dropLast(1).joinToString(".")
        }
    }
    logger.info(
        "Looking under package [{}] for interface {}",
        packageToScan,
        interfaceClass.name,
    )

    // Convert package path to resource path
    val packageSearchPath = "classpath*:" +
            ClassUtils.convertClassNameToResourcePath(packageToScan) +
            "/**/*.class"

    try {
        // Find all class resources in the specified package
        val resources = resolver.getResources(packageSearchPath)

        for (resource in resources) {
            if (resource.isReadable) {
                val metadataReader: MetadataReader = metadataReaderFactory.getMetadataReader(resource)
                val className = metadataReader.classMetadata.className

                try {
                    val candidateClass = Class.forName(className)

                    // Check if class is a non-interface, non-abstract class that implements our interface
                    if (!candidateClass.isInterface &&
                        !java.lang.reflect.Modifier.isAbstract(candidateClass.modifiers) &&
                        interfaceClass.isAssignableFrom(candidateClass)
                    ) {

                        @Suppress("UNCHECKED_CAST")
                        implementations.add(candidateClass as Class<out T>)
                    }
                } catch (e: Exception) {
                    // Skip classes that can't be loaded
                    println("Failed to load class: $className, reason: ${e.message}")
                }
            }
        }
    } catch (e: Exception) {
        println("Error scanning classpath: ${e.message}")
    }

    return implementations
}
