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
package com.embabel.common.test.neo

import com.embabel.test.neo.NeoIntegrationTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jTemplate

/**
 * Convenient superclass for Neo transactional integration tests using Testcontainers.
 * Test effects are rolled back by Spring test infrastructure.
 */
@NeoIntegrationTest
open class NeoIntegrationTestSupport {

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var neo4jClient: Neo4jClient

    @Autowired
    protected lateinit var neo4jTemplate: Neo4jTemplate

    @Autowired
    protected lateinit var resourceLoader: ResourceLoader

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

}
