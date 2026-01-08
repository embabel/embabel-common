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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles


/**
 * Convenient and efficient way to provide a logger for a specific class.
 * Useful when we don't want to hold a field--for example, in an entity or utility function.
 */
inline fun <reified T> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)


/**
 * Provide a logger for any class.
 */
@Deprecated("Use the inline version for better performance", ReplaceWith("loggerFor<Type>()"))
fun logger(): Logger {
    val callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        .walk { frames ->
            frames
                .filter { !it.className.contains("GetLogger") }
                .findFirst()
                .map { it.declaringClass }
                .orElse(MethodHandles.lookup().lookupClass())
        }
    return LoggerFactory.getLogger(callerClass)
}
