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

/**
 * Minimal conversation interface for storage-agnostic persistence.
 *
 * This interface captures the essential properties needed to persist a conversation
 * without coupling to agent-specific types like PromptContributor or AssetTracker.
 *
 * Implementations in agent frameworks (e.g., embabel-agent) can extend this
 * interface with richer functionality while still being storable.
 */
interface StorableConversation {

    /**
     * Unique identifier for this conversation.
     */
    val id: String

    /**
     * Messages in the conversation in chronological order.
     */
    val messages: List<StorableMessage>

    /**
     * Whether this conversation is backed by persistent storage.
     *
     * Returns `true` for database-backed conversations, `false` for in-memory.
     */
    fun persistent(): Boolean = false

    /**
     * Add a message to the conversation.
     *
     * @param message the message to add
     * @return the added message (may be wrapped or enhanced by the implementation)
     */
    fun addMessage(message: StorableMessage): StorableMessage

    /**
     * Add a message with explicit author attribution.
     *
     * @param message the message to add
     * @param author the author of this message
     * @return the added message
     */
    fun addMessageFrom(message: StorableMessage, author: MessageAuthor?): StorableMessage =
        addMessage(message)

    /**
     * Add a message with explicit author and recipient.
     *
     * @param message the message to add
     * @param from the author of this message
     * @param to the recipient of this message
     * @return the added message
     */
    fun addMessageFromTo(
        message: StorableMessage,
        from: MessageAuthor?,
        to: MessageAuthor?
    ): StorableMessage = addMessage(message)

    /**
     * Create a view of this conversation with only the last n messages.
     *
     * Implementations may optimize this (e.g., database query with LIMIT)
     * or use the default in-memory slicing.
     *
     * @param n the number of messages to include
     * @return a conversation view with the last n messages
     */
    fun last(n: Int): StorableConversation = SlicedStorableConversation(
        id = id,
        slicedMessages = messages.takeLast(n)
    )
}

/**
 * A read-only slice of a conversation with a subset of messages.
 */
private class SlicedStorableConversation(
    override val id: String,
    private val slicedMessages: List<StorableMessage>
) : StorableConversation {

    override val messages: List<StorableMessage>
        get() = slicedMessages

    override fun persistent(): Boolean = false

    override fun addMessage(message: StorableMessage): StorableMessage {
        throw UnsupportedOperationException("Cannot add messages to a conversation slice")
    }

    override fun last(n: Int): StorableConversation = SlicedStorableConversation(
        id = id,
        slicedMessages = slicedMessages.takeLast(n)
    )
}
