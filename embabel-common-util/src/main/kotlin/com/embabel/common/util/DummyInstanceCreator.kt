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

import com.embabel.common.util.findImplementationsOnClasspath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.ParameterizedType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


/**
 * Create mock instances of objects.
 * This is useful for testing purposes and for creating
 * instances we may want to introspect.
 * @param stringsToUse array of string choices for String fields.
 * Defaults to lorem ipsum values
 */
open class DummyInstanceCreator(
    protected val stringsToUse: List<String> = LoremIpsums,
) {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val random = Random()

    /**
     * Create a dummy instance of this class
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> createDummyInstance(clazz: Class<T>): T {
        logger.debug("Creating mock instance for class {}", clazz.name)
        // Handle primitive types and common Java types
        when {
            clazz == String::class.java -> return getRandomString() as T
            clazz == Int::class.java || clazz == Integer::class.java -> return random.nextInt(1000) as T
            clazz == Long::class.java -> return random.nextLong() as T
            clazz == Double::class.java -> return (random.nextDouble() * 100) as T
            clazz == Float::class.java -> return (random.nextFloat() * 100) as T
            clazz == Boolean::class.java -> return random.nextBoolean() as T
            clazz == List::class.java -> return listOf(getRandomString()) as T
            clazz.isEnum -> return (clazz.enumConstants?.let { it[random.nextInt(it.size)] } ?: "") as T
            clazz == LocalDate::class.java -> return LocalDate.now() as T
            clazz == LocalDateTime::class.java -> return LocalDateTime.now() as T
            clazz == Instant::class.java -> return Instant.now() as T
            clazz == Date::class.java -> return Date() as T
            clazz == Duration::class.java -> return Duration.ofMillis(random.nextLong()) as T
        }

        // Handle List<T> with reflection
        if (List::class.java.isAssignableFrom(clazz)) {
            val genericType = findListGenericType(clazz)
            genericType?.let { componentType ->
                return createDummyList(componentType, 3) as T
            }
        }

        // For data classes and other complex types, attempt to find a constructor
        val constructors = clazz.declaredConstructors
        if (constructors.isEmpty()) {
            if (clazz.isInterface) {
                // Try to find an implementation
                val implementations = findImplementationsOnClasspath(clazz)
                val impl = implementations.random()
                logger.info("Using implementation {} of interface {}", impl.name, clazz.name)
                return createDummyInstance(impl)
            }
            throw IllegalArgumentException("No constructor found for class: ${clazz.name}")
        }

        // Choose the constructor with the most parameters
        val constructor = constructors.maxByOrNull { it.parameterCount } ?: constructors[0]
        constructor.isAccessible = true

        // Create arguments for the constructor
        val parameterTypes = constructor.genericParameterTypes
        val arguments = parameterTypes.map { paramType ->
            when {
                paramType is ParameterizedType -> {
                    val rawType = paramType.rawType as Class<*>
                    if (List::class.java.isAssignableFrom(rawType)) {
                        val componentType = paramType.actualTypeArguments[0]
                        if (componentType is Class<*>) {
                            createDummyList(componentType, 3)
                        } else {
                            emptyList<Any>()
                        }
                    } else {
                        // For other generic types, try to create a mock instance
                        createDummyInstance(rawType)
                    }
                }

                paramType is Class<*> -> createDummyInstance(paramType)
                else -> null // Default for unknown types
            }
        }.toTypedArray()

        // Create a new instance with the constructor
        return constructor.newInstance(*arguments) as T
    }

    private fun findListGenericType(clazz: Class<*>): Class<*>? {
        val genericInterfaces = clazz.genericInterfaces
        for (genericInterface in genericInterfaces) {
            if (genericInterface is ParameterizedType) {
                val rawType = genericInterface.rawType as Class<*>
                if (List::class.java.isAssignableFrom(rawType)) {
                    val typeArg = genericInterface.actualTypeArguments[0]
                    if (typeArg is Class<*>) {
                        return typeArg
                    }
                }
            }
        }
        return null
    }

    private fun <T> createDummyList(componentType: Class<T>, size: Int): List<T> {
        return (0 until size).map { createDummyInstance(componentType) }
    }

    private fun getRandomString(): String =
        stringsToUse.random()

    companion object {

        val LoremIpsums = listOf(
            "Lorem ipsum dolor sit amet", "consectetur adipiscing elit", "sed do eiusmod tempor",
            "incididunt ut labore", "et dolore magna aliqua", "Ut enim ad minim veniam",
            "quis nostrud exercitation", "ullamco laboris nisi", "ut aliquip ex ea commodo",
            "consequat Duis aute", "irure dolor in reprehenderit", "in voluptate velit esse",
            "cillum dolore eu fugiat", "nulla pariatur Excepteur", "sint occaecat cupidatat",
            "non proident sunt", "in culpa qui officia", "deserunt mollit anim", "id est laborum"
        )

        val BigLebowskiQuotes = listOf(
            "This is not Nam", "This is bowling", "There are rules",
            "The Dude abides", "That rug really tied the room together",
            "Obviously you're not a golfer", "Careful man, there's a beverage here",
            "I'm the Dude", "Far out", "This aggression will not stand",
            "You're out of your element", "Shut up Donny", "Mark it zero",
            "It really tied the room together", "New information has come to light",
            "That's just like, your opinion, man", "The Dude minds"
        )

        /**
         * A dummy instance creator that uses Lorem Ipsum strings
         */
        val LoremIpsum = DummyInstanceCreator(
            stringsToUse = LoremIpsums,
        )

        /**
         * A dummy instance creator that uses Big Lebowski quotes
         */
        val BigLebowski = DummyInstanceCreator(
            stringsToUse = BigLebowskiQuotes,
        )
    }
}
