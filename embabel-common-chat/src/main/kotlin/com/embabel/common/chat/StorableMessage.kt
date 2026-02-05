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
package com.embabel.common.chat

import java.time.Instant

/**
 * Minimal message interface for storage-agnostic persistence.
 *
 * This interface captures the essential properties needed to persist a message
 * without coupling to agent-specific types like Awaitable or ActionContext.
 *
 * Implementations in agent frameworks (e.g., embabel-agent) can extend this
 * interface with richer functionality while still being storable.
 */
interface StorableMessage {

    /**
     * The role of the message sender.
     */
    val role: MessageRole

    /**
     * The text content of the message.
     */
    val content: String

    /**
     * When the message was created.
     */
    val timestamp: Instant

    /**
     * Optional identifier of the message author.
     * Useful for multi-user conversations.
     */
    val authorId: String?
        get() = null

    /**
     * Optional identifier of the message recipient.
     * Useful for directed messages in group chats.
     */
    val recipientId: String?
        get() = null
}

/**
 * Simple implementation of [StorableMessage] for basic use cases.
 */
data class SimpleStorableMessage(
    override val role: MessageRole,
    override val content: String,
    override val timestamp: Instant = Instant.now(),
    override val authorId: String? = null,
    override val recipientId: String? = null
) : StorableMessage
