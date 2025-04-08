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

import org.junit.jupiter.api.Assertions.assertEquals
import org.neo4j.driver.internal.InternalNode
import org.springframework.data.neo4j.core.Neo4jClient

fun nodeAliases(neo4jClient: Neo4jClient, label: String = "Scientist", name: String = "Marie"): Set<String> {
    val aliases = neo4jClient
        .query(
            """
            MATCH (n:$label { name: '$name' })
            UNWIND n.aliases AS alias
            RETURN alias
            """.trimIndent()
        )
        .fetchAs(String::class.java)
        .all()
    return aliases.toSet()
}

fun countByName(label: String, name: String, neo4jClient: Neo4jClient): Int {
    val marieCount = neo4jClient.query(
        """
             MATCH (n:$label {name: '$name'})
             RETURN count(n) as count
             """.trimIndent()
    )
        .fetchAs(Int::class.java)
        .one()
        .orElseThrow()
    return marieCount
}

fun countByLabel(label: String, neo4jClient: Neo4jClient): Int {
    val marieCount = neo4jClient.query(
        """
             MATCH (n:$label)
             RETURN count(n) as count
             """.trimIndent()
    )
        .fetchAs(Int::class.java)
        .one()
        .orElseThrow()
    return marieCount
}

fun findByLabel(label: String, neo4jClient: Neo4jClient): Collection<InternalNode> {
    val r = neo4jClient.query(
        """
             MATCH (n:$label)
             RETURN n
             """.trimIndent()
    ).fetch().all()
    return r.map {
        it?.get("n") as InternalNode
    }
}

fun assertNumberOfEntities(label: String, expected: Int, neo4jClient: Neo4jClient) {
    val found = findByLabel(label, neo4jClient)
    assertEquals(
        expected, found.size, "Should have $expected $label in the database: Have\n${
            found.joinToString("\n") { "\t${it.labels()}, id=${it.get("id")}" }
        }"
    )
}
