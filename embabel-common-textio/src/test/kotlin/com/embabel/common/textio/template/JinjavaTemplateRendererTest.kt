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
}
