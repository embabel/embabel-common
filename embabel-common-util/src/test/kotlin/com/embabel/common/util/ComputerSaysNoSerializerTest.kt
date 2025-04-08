/*
* Copyright 2025 Embabel Software, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.embabel.common.util

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

@JsonSerialize(using = ComputerSaysNoSerializer::class)
internal class NoWay {
    @JsonProperty
    var i: Int = 0
}

internal class YesWay {
    @JsonProperty
    var i: Int = 0
}

internal class ComputerSaysNoSerializerTest {
    private val objectMapper = ObjectMapper()

    @Test
    @Throws(JsonProcessingException::class)
    fun testCanSerialize() {
        val yesWay = YesWay()
        objectMapper.writeValueAsString(yesWay)
    }

    @Test
    fun testCannotSerialize() {
        val noWay = NoWay()
        Assertions.assertThrows<JsonMappingException?>(
            JsonMappingException::class.java,
            Executable { objectMapper.writeValueAsString(noWay) })
    }
}
