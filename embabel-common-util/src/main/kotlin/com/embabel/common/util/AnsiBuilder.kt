/*
* Copyright 2025 Embabel Software, Inc.
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


fun hexToRgb(hexValue: Int): Triple<Int, Int, Int> {
    // Extract RGB components from the integer value
    val r = ((hexValue shr 16) and 0xFF).toInt()
    val g = ((hexValue shr 8) and 0xFF).toInt()
    val b = (hexValue and 0xFF).toInt()

    return Triple(r, g, b)
}

// ANSI color codes
object AnsiColor {
    const val BLACK = "30"
    const val RED = "31"
    const val GREEN = "32"
    const val YELLOW = "33"
    const val BLUE = "34"
    const val MAGENTA = "35"
    const val CYAN = "36"
    const val WHITE = "37"

    // Bright variants
    const val BRIGHT_BLACK = "90"
    const val BRIGHT_RED = "91"
    const val BRIGHT_GREEN = "92"
    const val BRIGHT_YELLOW = "93"
    const val BRIGHT_BLUE = "94"
    const val BRIGHT_MAGENTA = "95"
    const val BRIGHT_CYAN = "96"
    const val BRIGHT_WHITE = "97"
}

// ANSI style codes
object AnsiStyle {
    const val BOLD = "1"
    const val DIM = "2"
    const val ITALIC = "3"
    const val UNDERLINE = "4"
    const val BLINK = "5"
    const val REVERSE = "7"
    const val HIDDEN = "8"
}

// Background colors
object AnsiBgColor {
    const val BG_BLACK = "40"
    const val BG_RED = "41"
    const val BG_GREEN = "42"
    const val BG_YELLOW = "43"
    const val BG_BLUE = "44"
    const val BG_MAGENTA = "45"
    const val BG_CYAN = "46"
    const val BG_WHITE = "47"

    // Bright variants
    const val BG_BRIGHT_BLACK = "100"
    const val BG_BRIGHT_RED = "101"
    const val BG_BRIGHT_GREEN = "102"
    const val BG_BRIGHT_YELLOW = "103"
    const val BG_BRIGHT_BLUE = "104"
    const val BG_BRIGHT_MAGENTA = "105"
    const val BG_BRIGHT_CYAN = "106"
    const val BG_BRIGHT_WHITE = "107"
}

/**
 * Applies ANSI formatting with the specified style codes
 * @param text The text to format
 * @param styles ANSI style/color codes to apply
 * @return The formatted text with ANSI escape sequences
 */
fun ansi(text: String, vararg styles: String): String {
    val start = "\u001B[${styles.joinToString(";")}m"
    val reset = "\u001B[0m"
    return "$start$text$reset"
}

/**
 * Class for building nested ANSI formatting without reset issues
 */
class AnsiBuilder {
    private val styles = mutableListOf<String>()

    /**
     * Adds styles to the current style stack
     */
    fun withStyle(vararg newStyles: String): AnsiBuilder {
        styles.addAll(newStyles)
        return this
    }

    /**
     * Applies all current styles to the text
     */
    fun format(text: String): String {
        if (styles.isEmpty()) return text
        val styleStr = styles.joinToString(";")
        return "\u001B[${styleStr}m$text\u001B[0m"
    }

    /**
     * Creates a copy of the current builder
     */
    fun copy(): AnsiBuilder {
        val newBuilder = AnsiBuilder()
        newBuilder.styles.addAll(this.styles)
        return newBuilder
    }

    /**
     * Combine multiple formatted strings while preserving each one's formatting
     */
    companion object {
        fun combine(vararg parts: String): String {
            return parts.joinToString("")
        }
    }
}

// Extension functions for String for more fluent formatting
fun String.color(color: String): String = ansi(this, color)

fun String.color(rgb: Triple<Int, Int, Int>): String {
    val (r, g, b) = rgb
    return "\u001B[38;2;$r;$g;${b}m$this\u001B[0m"
}

fun String.color(rgb: Int): String = color(hexToRgb(rgb))

fun String.bgColor(color: String): String = ansi(this, color)
fun String.bold(): String = ansi(this, AnsiStyle.BOLD)
fun String.italic(): String = ansi(this, AnsiStyle.ITALIC)
fun String.underline(): String = ansi(this, AnsiStyle.UNDERLINE)

// Safe concatenation function for formatted text
fun concatFormatted(vararg parts: String): String {
    return parts.joinToString("")
}

// Extension function for nested styles
fun String.styled(setup: AnsiBuilder.() -> AnsiBuilder): String {
    return setup(AnsiBuilder()).format(this)
}
