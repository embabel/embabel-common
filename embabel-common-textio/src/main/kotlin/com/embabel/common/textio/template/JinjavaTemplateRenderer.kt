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

import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import com.hubspot.jinjava.loader.ResourceLocator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.util.DigestUtils
import java.io.IOException
import java.nio.charset.Charset

data class JinjaProperties(
    val prefix: String,
    val suffix: String = ".jinja",
    val failOnUnknownTokens: Boolean = false,
)

/**
 * Wrap HubSpot Jinjava to render templates.
 * Files are expected to end with '.jinja'
 * Don't forget to escape anything that may be problematic with {{ title|e}} syntax
 */
class JinjavaTemplateRenderer(
    private val jinja: JinjaProperties = JinjaProperties("classpath:/prompts/", ".jinja", false),
    private val resourceLoader: ResourceLoader = DefaultResourceLoader(),
) : TemplateRenderer {
    private val logger: Logger = LoggerFactory.getLogger(JinjavaTemplateRenderer::class.java)

    /**
     * Render a template string without loading it
     *
     * @param template string template
     * @param model    model map
     * @return rendered string
     */
    @Throws(InvalidTemplateException::class)
    override fun renderLiteralTemplate(
        template: String,
        model: Map<String, Any>,
    ): String {
        try {
            val jcConfig = JinjavaConfig.newBuilder()
                .withFailOnUnknownTokens(jinja.failOnUnknownTokens)
                .withTrimBlocks(true)
                .build()
            return Jinjava(jcConfig).run {
                registerFilter(EscFilter())
                resourceLocator = SpringResourceLocator()
                render(template, model)
            }
        } catch (e: Exception) {
            throw InvalidTemplateException("Invalid template '$template'", e)
        }
    }

    @Throws(NoSuchTemplateException::class, InvalidTemplateException::class)
    override fun renderLoadedTemplate(templateName: String, model: Map<String, Any>): String {
        val template: String = load(templateName)
        try {
            return renderLiteralTemplate(template, model)
        } catch (ex: InvalidTemplateException) {
            throw InvalidTemplateException("Invalid template at '$templateName'", ex)
        }
    }

    /**
     * Expand the template name to a full path based on the Jinja properties.
     */
    private fun getLocation(template: String): String {
        // If it's a fully specified Spring resource, return it as is
        if (template.startsWith("classpath:") || template.startsWith("file:")) {
            return template
        }
        return jinja.prefix + template + if (template.contains(jinja.suffix)) "" else jinja.suffix
    }

    @Throws(NoSuchTemplateException::class)
    override fun load(templateName: String): String {
        val expanded = getLocation(templateName)
        val resource = resourceLoader.getResource(expanded)
        if (!resource.exists()) {
            throw NoSuchTemplateException(templateName, resource)
        }
        try {
            val template = resource.getContentAsString(Charset.defaultCharset())
            val sha = DigestUtils.md5DigestAsHex(template.toByteArray())
            logger.debug("Loaded template {} with sha [{}] at location [{}]", templateName, sha, expanded)
            return template
        } catch (e: IOException) {
            throw InvalidTemplateException("Can't read template at '$expanded'", e)
        }
    }

    private inner class SpringResourceLocator : ResourceLocator {
        override fun getString(fullName: String, encoding: Charset?, interpreter: JinjavaInterpreter?): String? {
            return load(fullName)
        }
    }

}

class EscFilter : Filter {
    override fun filter(
        obj: Any?,
        interpreter: JinjavaInterpreter?,
        vararg args: String?,
    ): Any? {
        return escapeForJinjava(obj as String?)
    }

    override fun getName(): String? =
        "esc"

}

/**
 * Escapes the content for safe use in Jinjava templates. It escapes the characters
 * '{' and '}' by replacing them with their HTML entity equivalents.
 *
 * @param content The string to be escaped.
 * @return The escaped string, safe for Jinjava templates.
 */
private fun escapeForJinjava(content: String?): String {
    if (content == null) {
        return ""
    }
    return content.replace("{", "&#123;").replace("}", "&#125;")
}
