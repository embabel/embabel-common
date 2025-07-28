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

import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.PlatformDefault
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.byName
import com.embabel.common.core.types.HasInfoString
import com.embabel.common.util.indent
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Thinking config. Set on Anthropic models
 * and some Ollama models.
 */
class Thinking private constructor(
    val enabled: Boolean = false,
    val tokenBudget: Int? = null,
) {

    companion object {

        @JvmStatic
        fun withTokenBudget(withTokenBudget: Int): Thinking = Thinking(
            enabled = true,
            tokenBudget = withTokenBudget,
        )

        val NONE: Thinking = Thinking(
            enabled = false,
        )
    }
}

/**
 * Common hyperparameters for LLMs.
 */
interface LlmHyperparameters {

    @get:Schema(
        description = "The temperature to use when generating responses",
        minimum = "0.0",
        maximum = "1.0",
        defaultValue = "0.0",
        required = true,
    )
    val temperature: Double

    val frequencyPenalty: Double?

    val maxTokens: Int?

    val presencePenalty: Double?

    val topK: Int?

    val topP: Double?
}

/**
 * Portable LLM options.
 */
@Schema(
    description = "Options for LLM use",
)
@JsonDeserialize(`as` = BuildableLlmOptions::class)
interface LlmOptions : LlmHyperparameters, HasInfoString {

    @get:Schema(
        description = "If provided, custom selection criteria for the LLM to use. If not provided, a default LLM will be used.",
        required = false,
    )
    val criteria: ModelSelectionCriteria?

    val thinking: Thinking?

    override fun infoString(
      verbose: Boolean?,
      indent: Int,
    ): String {
        return toString().indent(indent)
    }

    companion object {

        /**
         * Create an LlmOptions instance we can build.
         */
        @JvmOverloads
        operator fun invoke(
            temperature: Double = DEFAULT_TEMPERATURE,
        ): BuildableLlmOptions = BuildableLlmOptions(
            criteria = PlatformDefault,
            temperature = temperature,
        )

        /**
         * Create an LlmOptions instance we can build.
         */
        @JvmOverloads
        operator fun invoke(
            model: String,
            temperature: Double = DEFAULT_TEMPERATURE,
        ): BuildableLlmOptions = BuildableLlmOptions(
            criteria = ByNameModelSelectionCriteria(model),
            temperature = temperature,
        )

        @JvmStatic
        fun withDefaults(): BuildableLlmOptions = BuildableLlmOptions(
            criteria = PlatformDefault,
            temperature = DEFAULT_TEMPERATURE,
        )

        @JvmOverloads
        @JvmStatic
        fun fromModel(
            model: String,
            temperature: Double = DEFAULT_TEMPERATURE,
        ): BuildableLlmOptions = BuildableLlmOptions(
            criteria = byName(model),
            temperature = temperature,
        )

        @JvmOverloads
        operator fun invoke(
            criteria: ModelSelectionCriteria,
            temperature: Double = DEFAULT_TEMPERATURE,
        ): BuildableLlmOptions = BuildableLlmOptions(
            criteria = criteria,
            temperature = temperature,
        )

        @JvmOverloads
        @JvmStatic
        fun fromCriteria(
            criteria: ModelSelectionCriteria,
            temperature: Double = DEFAULT_TEMPERATURE,
        ): BuildableLlmOptions = BuildableLlmOptions(
            criteria = criteria,
            temperature = temperature,
        )

        const val DEFAULT_TEMPERATURE = 0.5
    }

}

data class BuildableLlmOptions(
    override val criteria: ModelSelectionCriteria = PlatformDefault,
    override val temperature: Double = 0.0,
    override val frequencyPenalty: Double? = null,
    override val maxTokens: Int? = null,
    override val presencePenalty: Double? = null,
    override val topK: Int? = null,
    override val topP: Double? = null,
    override val thinking: Thinking? = null,
) : LlmOptions {

    fun withTemperature(temperature: Double): BuildableLlmOptions {
        return copy(temperature = temperature)
    }

    fun withModel(model: String): BuildableLlmOptions {
        return copy(criteria = criteria)
    }

    fun withMaxTokens(maxTokens: Int): BuildableLlmOptions {
        return copy(maxTokens = maxTokens)
    }

    fun withTopK(topK: Int): BuildableLlmOptions {
        return copy(topK = topK)
    }

    fun withTopP(topP: Double): BuildableLlmOptions {
        return copy(topP = topP)
    }

    fun withFrequencyPenalty(frequencyPenalty: Double): BuildableLlmOptions {
        return copy(frequencyPenalty = frequencyPenalty)
    }

    fun withPresencePenalty(presencePenalty: Double): BuildableLlmOptions {
        return copy(presencePenalty = presencePenalty)
    }

    fun withThinking(thinking: Thinking): BuildableLlmOptions {
        return copy(thinking = thinking)
    }

    fun withoutThinking(): BuildableLlmOptions {
        return copy(thinking = Thinking.NONE)
    }

}
