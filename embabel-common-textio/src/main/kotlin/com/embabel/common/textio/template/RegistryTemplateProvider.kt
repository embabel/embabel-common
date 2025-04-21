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

/**
 * Provides 1:1 mapping of logical names to templates.
 */
data class RegistryTemplateProvider(
    private val templateRenderer: TemplateRenderer,
) : TemplateProvider {

    private val registry: MutableMap<String, CompiledTemplate> = mutableMapOf()

    fun withTemplate(logicalName: String, location: String): RegistryTemplateProvider {
        registry[logicalName] = templateRenderer.compileLoadedTemplate(location)
        return this
    }

    override fun resolveTemplate(logicalName: String): CompiledTemplate = registry[logicalName]
        ?: throw NoSuchTemplateException("Cannot find logical template $logicalName: known logical names are ${registry.keys}")
}
