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

import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.Resource

/**
 * Can compile reusable templates.
 */
interface TemplateCompiler {

    /**
     * Create a reusable template
     */
    fun compileLoadedTemplate(templateName: String): CompiledTemplate
}

/**
 * Object that can render templates with a model. Methods throw unchecked exceptions if
 * the template is not found or is invalid. The exceptions defined in this interface will
 * wrap exceptions from an underlying implementation.
 * "loaded" methods load Spring Resource's,
 * while "literal" methods take a string literal
 */
interface TemplateRenderer : TemplateCompiler {

    /**
     * Load the template. Useful if we have to introspect it
     * @param templateName name of the template
     * @return template content
     * @throws NoSuchTemplateException if we can't find the template
     */
    @Throws(NoSuchTemplateException::class)
    fun load(templateName: String): String

    /**
     * Render a template with the given model
     * @param templateName template to use. Path will be expanded based on the
     * implementation and configuration
     * @param model model
     * @return string result of rendering string
     */
    @Throws(NoSuchTemplateException::class, InvalidTemplateException::class)
    fun renderLoadedTemplate(templateName: String, model: Map<String, Any>): String

    /**
     * Render a template string without loading it
     * @param template template as string literal
     * @param model model to render
     * @return rendered string
     */
    @Throws(InvalidTemplateException::class)
    fun renderLiteralTemplate(template: String, model: Map<String, Any>): String

    override fun compileLoadedTemplate(templateName: String): CompiledTemplate {
        return TemplateRendererCompiledTemplate(templateName, this)
    }
}

/**
 * Reusable template
 */
interface CompiledTemplate {

    val name: String

    fun render(model: Map<String, Any>): String
}

private class TemplateRendererCompiledTemplate(
    private val templateName: String,
    private val renderer: TemplateRenderer,
) : CompiledTemplate {
    override fun render(model: Map<String, Any>): String =
        renderer.renderLoadedTemplate(templateName, model)

    override val name: String = templateName
}

class NoSuchTemplateException(message: String) : RuntimeException(message) {
    constructor(templateName: String, resource: Resource) : this("Template [$templateName] not found using $resource")
}

/**
 * Thrown when a template is invalid
 */
class InvalidTemplateException(message: String, cause: Throwable) : NestedRuntimeException(message, cause)
