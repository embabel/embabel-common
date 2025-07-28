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
package com.embabel.common.ai.model

import com.embabel.common.core.types.HasInfoString
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


class NoSuitableModelException(criteria: ModelSelectionCriteria, models: List<AiModel<*>>) :
    RuntimeException(
        """
        No suitable model found for $criteria.
        ${models.size} models available: ${models.map { it.name }}
        """
    )

/**
 * Superinterface for model selection criteria
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.SIMPLE_NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ByNameModelSelectionCriteria::class),
    JsonSubTypes.Type(value = ByRoleModelSelectionCriteria::class),
    JsonSubTypes.Type(value = RandomByNameModelSelectionCriteria::class),
    JsonSubTypes.Type(value = FallbackByNameModelSelectionCriteria::class),
    JsonSubTypes.Type(value = AutoModelSelectionCriteria::class),
    JsonSubTypes.Type(value = DefaultModelSelectionCriteria::class),
    JsonSubTypes.Type(value = ByNameModelSelectionCriteria::class),
)
sealed interface ModelSelectionCriteria {

    companion object {

        @JvmStatic
        fun byRole(role: String): ModelSelectionCriteria = ByRoleModelSelectionCriteria(role)

        @JvmStatic
        fun byName(name: String): ModelSelectionCriteria = ByNameModelSelectionCriteria(name)

        @JvmStatic
        fun randomOf(vararg names: String): ModelSelectionCriteria =
            RandomByNameModelSelectionCriteria(names.toList())

        @JvmStatic
        fun firstOf(vararg names: String): ModelSelectionCriteria =
            FallbackByNameModelSelectionCriteria(names.toList())

        /**
         * Choose an LLM automatically. Rely on platform
         * to do the right thing.
         */
        @JvmStatic
        val Auto: ModelSelectionCriteria = AutoModelSelectionCriteria

        @JvmStatic
        val PlatformDefault: ModelSelectionCriteria = DefaultModelSelectionCriteria
    }
}

/**
 * Select an LLM by role
 */
data class ByRoleModelSelectionCriteria(
    val role: String,
) : ModelSelectionCriteria

data class ByNameModelSelectionCriteria(
    val name: String,
) : ModelSelectionCriteria

data class RandomByNameModelSelectionCriteria(
    val names: List<String>,
) : ModelSelectionCriteria

data class FallbackByNameModelSelectionCriteria(
    val names: List<String>,
) : ModelSelectionCriteria

/**
 * Choose an LLM automatically: For example, in a platform, based
 * on runtime analysis, or based on analysis of the prompt
 */
object AutoModelSelectionCriteria : ModelSelectionCriteria {
    override fun toString(): String = "AutoModelSelectionCriteria"
}

object DefaultModelSelectionCriteria : ModelSelectionCriteria {
    override fun toString(): String = "DefaultModelSelectionCriteria"
}

/**
 * Provide AI models for requested roles, and expose data about available models.
 */
interface ModelProvider : HasInfoString {

    @Throws(NoSuitableModelException::class)
    fun getLlm(criteria: ModelSelectionCriteria): Llm

    @Throws(NoSuitableModelException::class)
    fun getEmbeddingService(criteria: ModelSelectionCriteria): EmbeddingService

    /**
     * List the roles available for this class of model
     */
    fun listRoles(modelClass: Class<out AiModel<*>>): List<String>

    fun listModelNames(modelClass: Class<out AiModel<*>>): List<String>

    fun listModels(): List<ModelMetadata>

    /**
     * Well-known roles for models
     * Useful but not exhaustive: users are free to define their own roles
     * @see ByRoleModelSelectionCriteria
     */
    companion object {

        const val BEST_ROLE = "best"

        const val CHEAPEST_ROLE = "cheapest"

    }

}
