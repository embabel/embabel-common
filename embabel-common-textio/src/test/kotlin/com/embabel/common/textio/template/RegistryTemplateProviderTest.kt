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
package com.embabel.common.textio.template

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
* Tests the template registry functionality:
* - Verifies template addition and retrieval
* - Validates error handling for missing templates
* - Tests method chaining
 */
class RegistryTemplateProviderTest {

    private lateinit var templateRenderer: TemplateRenderer
    private lateinit var compiledTemplate: CompiledTemplate
    private lateinit var registryTemplateProvider: RegistryTemplateProvider

    @BeforeEach
    fun setUp() {
        templateRenderer = mockk()
        compiledTemplate = mockk()

        every { templateRenderer.compileLoadedTemplate("template1") } returns compiledTemplate
        every { templateRenderer.compileLoadedTemplate("template2") } returns compiledTemplate

        registryTemplateProvider = RegistryTemplateProvider(templateRenderer)
    }

    @Test
    fun `withTemplate should add template to registry and return self`() {
        val result = registryTemplateProvider.withTemplate("logical1", "template1")

        assertEquals(registryTemplateProvider, result, "Should return self for chaining")
        verify { templateRenderer.compileLoadedTemplate("template1") }
    }

    @Test
    fun `withTemplate should allow chaining multiple templates`() {
        registryTemplateProvider
            .withTemplate("logical1", "template1")
            .withTemplate("logical2", "template2")

        verify { templateRenderer.compileLoadedTemplate("template1") }
        verify { templateRenderer.compileLoadedTemplate("template2") }
    }

    @Test
    fun `resolveTemplate should return compiled template when logical name exists`() {
        registryTemplateProvider.withTemplate("logical1", "template1")

        val result = registryTemplateProvider.resolveTemplate("logical1")

        assertEquals(compiledTemplate, result)
    }

    @Test
    fun `resolveTemplate should throw NoSuchTemplateException when logical name doesn't exist`() {
        registryTemplateProvider.withTemplate("logical1", "template1")

        val exception = assertThrows<NoSuchTemplateException> {
            registryTemplateProvider.resolveTemplate("nonexistent")
        }

        assert(exception.message!!.contains("nonexistent"))
        assert(exception.message!!.contains("logical1"))
    }
}
