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
package com.embabel.common.textio.template

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import java.nio.charset.Charset

/**
 *     Tests template loading and rendering:
 *     - Covers error cases (invalid templates, missing files)
 *     - Tests template conditional logic
 *     - Tests the template escaping filter
 *     - Includes an integration test with a real template
 *
 */
class JinjavaTemplateRendererTest {

    private lateinit var resourceLoader: ResourceLoader
    private lateinit var resource: Resource
    private lateinit var renderer: JinjavaTemplateRenderer
    private lateinit var jinjaProperties: JinjaProperties

    @BeforeEach
    fun setUp() {
        resourceLoader = mockk<ResourceLoader>()
        resource = mockk<Resource>()

        jinjaProperties = JinjaProperties(
            prefix = "classpath:/prompts/",
            suffix = ".jinja",
            failOnUnknownTokens = false
        )

        renderer = JinjavaTemplateRenderer(
            jinja = jinjaProperties,
            resourceLoader = resourceLoader
        )
    }

    @Test
    fun `load should retrieve template content`() {
        val templateContent = "Hello {{ name }}!"

        every { resourceLoader.getResource("classpath:/prompts/test-template.jinja") } returns resource
        every { resource.exists() } returns true
        every { resource.getContentAsString(Charset.defaultCharset()) } returns templateContent

        val result = renderer.load("test-template")

        assertEquals(templateContent, result)
        verify { resourceLoader.getResource("classpath:/prompts/test-template.jinja") }
    }

    @Test
    fun `load should throw NoSuchTemplateException when template doesn't exist`() {
        every { resourceLoader.getResource("classpath:/prompts/nonexistent.jinja") } returns resource
        every { resource.exists() } returns false

        assertThrows<NoSuchTemplateException> {
            renderer.load("nonexistent")
        }
    }

    @Test
    fun `renderLiteralTemplate should process template with model`() {
        val template = "Hello {{ name }}!"
        val model = mapOf("name" to "World")

        val result = renderer.renderLiteralTemplate(template, model)

        assertEquals("Hello World!", result)
    }

    @Test
    fun `renderLiteralTemplate should handle conditional blocks`() {
        val template = """
            Hello {{ name }}!
            {% if showDetails %}
            Details: {{ details }}
            {% endif %}
        """.trimIndent()

        val modelWithDetails = mapOf(
            "name" to "World",
            "showDetails" to true,
            "details" to "Important information"
        )

        val modelWithoutDetails = mapOf(
            "name" to "World",
            "showDetails" to false
        )

        val resultWithDetails = renderer.renderLiteralTemplate(template, modelWithDetails)
        val resultWithoutDetails = renderer.renderLiteralTemplate(template, modelWithoutDetails)

        assert(resultWithDetails.contains("Details: Important information"))
        assert(!resultWithoutDetails.contains("Details"))
    }

    /**
     * Only FATAL error results in throwing exception.
     * For example, reference to non-existing model, such as <code>fatal</code>
     */
    @Test
    fun `renderLiteralTemplate should throw InvalidTemplateException for invalid templates`() {
        val invalidTemplate = "Hello {{ name %fatal }}"
        val model = mapOf("name" to "World")

        assertThrows<InvalidTemplateException> {
            renderer.renderLiteralTemplate(invalidTemplate, model)
        }
    }

    @Test
    fun `renderLoadedTemplate should load and render template using full path`() {
        renderLoadedTemplateShouldLoadAndRenderTemplate("classpath:/prompts/test-template.jinja")
    }

    @Test
    fun `renderLoadedTemplate should load and render template using short path without extension`() {
        renderLoadedTemplateShouldLoadAndRenderTemplate("test-template")
    }

    @Test
    fun `renderLoadedTemplate should load and render template using short path with extension`() {
        renderLoadedTemplateShouldLoadAndRenderTemplate("test-template.jinja")
    }

    private fun renderLoadedTemplateShouldLoadAndRenderTemplate(location: String) {
        val templateContent = "Hello {{ name }}!"
        val model = mapOf("name" to "World")

        every { resourceLoader.getResource("classpath:/prompts/test-template.jinja") } returns resource
        every { resource.exists() } returns true
        every { resource.getContentAsString(Charset.defaultCharset()) } returns templateContent

        val result = renderer.renderLoadedTemplate(location, model)

        assertEquals("Hello World!", result)
    }

    @Test
    fun `compileLoadedTemplate should return a CompiledTemplate using full path`() {
        "classpath:/prompts/test-template.jinja"
    }

    @Test
    fun `compileLoadedTemplate should return a CompiledTemplate using short path`() {
        "test-template.jinja"
    }

    fun compileLoadedTemplateShouldReturnCompiledTemplate(location: String) {
        val templateContent = "Hello {{ name }}!"

        every { resourceLoader.getResource(location) } returns resource
        every { resource.exists() } returns true
        every { resource.getContentAsString(Charset.defaultCharset()) } returns templateContent

        val compiledTemplate = renderer.compileLoadedTemplate(location)

        assertNotNull(compiledTemplate)
        assertEquals("test-template", compiledTemplate.name)

        // Test rendering with the compiled template
        val model = mapOf("name" to "World")
        val result = compiledTemplate.render(model)

        assertEquals("Hello World!", result)
    }

    @Test
    fun `EscFilter should escape curly braces`() {
        val template = "This needs escaping: {{ content|esc }}"
        val model = mapOf("content" to "Example with {curly} braces")

        val result = renderer.renderLiteralTemplate(template, model)

        assertEquals("This needs escaping: Example with &#123;curly&#125; braces", result)
    }

    @Test
    fun `integration test with real resources`() {
        // Use actual resource loader for this test
        val realRenderer = JinjavaTemplateRenderer(
            jinja = JinjaProperties(prefix = "classpath:/prompts/", suffix = ".jinja"),
            resourceLoader = DefaultResourceLoader()
        )

        val model = mapOf(
            "name" to "World",
            "showDetails" to true,
            "details" to "Important information"
        )

        val result = realRenderer.renderLoadedTemplate("test-template", model)

        assert(result.contains("Hello World!"))
        assert(result.contains("With multiple lines"))
        assert(result.contains("Here are some details: Important information"))
    }

    @Test
    fun `error contains line number for unknown variable with strict mode`() {
        // Use strict mode renderer to ensure errors are thrown
        val strictRenderer = JinjavaTemplateRenderer(
            jinja = JinjaProperties(
                prefix = "classpath:/prompts/",
                suffix = ".jinja",
                failOnUnknownTokens = true
            ),
            resourceLoader = resourceLoader
        )

        val template = """
            |Line 1 is fine
            |Line 2 is fine
            |Line 3 has {{ unknownVariable }}
            |Line 4 is fine
        """.trimMargin()
        val model = emptyMap<String, Any>()

        val exception = assertThrows<InvalidTemplateException> {
            strictRenderer.renderLiteralTemplate(template, model)
        }

        // Verify that we have detailed errors
        assert(exception.errors.isNotEmpty()) { "Expected errors to be captured" }

        // Verify that line number is present
        val errorWithLine = exception.errors.find { it.lineNumber != null }
        assertNotNull(errorWithLine) { "Expected at least one error with a line number" }

        // Verify line number is correct (line 3)
        assertEquals(3, errorWithLine!!.lineNumber) { "Expected error on line 3" }

        // Verify the message mentions the line number
        assert(exception.message?.contains("Line 3") == true || exception.getDetailedMessage().contains("Line 3")) {
            "Expected error message to contain line number. Message: ${exception.message}"
        }
    }

    @Test
    fun `error contains field name for unknown variable with failOnUnknownTokens`() {
        val rendererWithStrictMode = JinjavaTemplateRenderer(
            jinja = JinjaProperties(
                prefix = "classpath:/prompts/",
                suffix = ".jinja",
                failOnUnknownTokens = true
            ),
            resourceLoader = resourceLoader
        )

        val template = "Hello {{ unknownVariable }}!"
        val model = emptyMap<String, Any>()

        val exception = assertThrows<InvalidTemplateException> {
            rendererWithStrictMode.renderLiteralTemplate(template, model)
        }

        // Verify that we have detailed errors
        assert(exception.errors.isNotEmpty()) { "Expected errors to be captured" }

        // Verify that the error message mentions the unknown variable
        val hasUnknownVarError = exception.errors.any { error ->
            error.message.contains("unknownVariable") || error.fieldName?.contains("unknownVariable") == true
        }
        assert(hasUnknownVarError) { "Expected error to mention 'unknownVariable'. Errors: ${exception.errors}" }
    }

    @Test
    fun `getDetailedMessage includes all error information`() {
        // Use strict mode renderer to ensure errors are thrown
        val strictRenderer = JinjavaTemplateRenderer(
            jinja = JinjaProperties(
                prefix = "classpath:/prompts/",
                suffix = ".jinja",
                failOnUnknownTokens = true
            ),
            resourceLoader = resourceLoader
        )

        val template = """
            |Line 1 is fine
            |Line 2 has {{ missingVar }}
        """.trimMargin()
        val model = emptyMap<String, Any>()

        val exception = assertThrows<InvalidTemplateException> {
            strictRenderer.renderLiteralTemplate(template, model)
        }

        val detailedMessage = exception.getDetailedMessage()

        // Verify the detailed message includes error information
        assert(detailedMessage.isNotBlank()) { "Expected non-blank detailed message" }
        assert(exception.errors.isNotEmpty()) { "Expected errors to be present" }

        // If we have errors with line numbers, verify they appear in detailed message
        exception.errors.filter { it.lineNumber != null }.forEach { error ->
            assert(detailedMessage.contains("Line ${error.lineNumber}")) {
                "Expected detailed message to contain line number ${error.lineNumber}. Message: $detailedMessage"
            }
        }
    }

    @Test
    fun `TemplateErrorDetail format includes all available fields`() {
        val error = TemplateErrorDetail(
            message = "Unknown token",
            lineNumber = 5,
            startPosition = 10,
            fieldName = "myVariable",
            severity = "FATAL"
        )

        val formatted = error.format()

        assert(formatted.contains("Line 5")) { "Expected line number in formatted output" }
        assert(formatted.contains("position 10")) { "Expected position in formatted output" }
        assert(formatted.contains("myVariable")) { "Expected field name in formatted output" }
        assert(formatted.contains("Unknown token")) { "Expected message in formatted output" }
        assert(formatted.contains("FATAL")) { "Expected severity in formatted output" }
    }

    @Test
    fun `TemplateErrorDetail format handles missing optional fields`() {
        val errorMinimal = TemplateErrorDetail(message = "Simple error")

        val formatted = errorMinimal.format()

        assertEquals("Simple error", formatted)
    }

    @Test
    fun `InvalidTemplateException maintains backward compatibility with cause constructor`() {
        val cause = RuntimeException("Original error")
        val exception = InvalidTemplateException("Template failed", cause)

        assertEquals("Template failed", exception.message)
        assertEquals(cause, exception.cause)
        assert(exception.errors.isEmpty()) { "Expected empty errors list for legacy constructor" }
    }

    @Test
    fun `InvalidTemplateException maintains backward compatibility with no-cause constructor`() {
        val exception = InvalidTemplateException("Template failed")

        assertEquals("Template failed", exception.message)
        assert(exception.errors.isEmpty()) { "Expected empty errors list for legacy constructor" }
    }

    @Test
    fun `renderLoadedTemplate preserves error details from renderLiteralTemplate`() {
        // Use strict mode renderer to ensure errors are thrown
        val strictRenderer = JinjavaTemplateRenderer(
            jinja = JinjaProperties(
                prefix = "classpath:/prompts/",
                suffix = ".jinja",
                failOnUnknownTokens = true
            ),
            resourceLoader = resourceLoader
        )

        val invalidTemplate = "Line 1\nLine 2 {{ unknownVar }}"

        every { resourceLoader.getResource("classpath:/prompts/error-template.jinja") } returns resource
        every { resource.exists() } returns true
        every { resource.getContentAsString(Charset.defaultCharset()) } returns invalidTemplate

        val exception = assertThrows<InvalidTemplateException> {
            strictRenderer.renderLoadedTemplate("error-template", emptyMap())
        }

        // Verify that errors are preserved through the renderLoadedTemplate call
        assert(exception.errors.isNotEmpty()) { "Expected errors to be preserved" }
        assert(exception.message?.contains("error-template") == true) { "Expected template name in message" }
    }

    @Test
    fun `multiple errors are captured and reported`() {
        // Use strict mode renderer to ensure errors are thrown
        val strictRenderer = JinjavaTemplateRenderer(
            jinja = JinjaProperties(
                prefix = "classpath:/prompts/",
                suffix = ".jinja",
                failOnUnknownTokens = true
            ),
            resourceLoader = resourceLoader
        )

        // Template with multiple unknown variables on different lines
        val template = """
            |{{ unknownVar1 }}
            |{{ unknownVar2 }}
        """.trimMargin()
        val model = emptyMap<String, Any>()

        val exception = assertThrows<InvalidTemplateException> {
            strictRenderer.renderLiteralTemplate(template, model)
        }

        // We should have at least one error
        assert(exception.errors.isNotEmpty()) { "Expected at least one error to be captured" }
    }
}
