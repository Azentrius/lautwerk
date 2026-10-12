// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import dev.openautoai.core.ai.ChatClient
import dev.openautoai.core.ai.ChatError
import dev.openautoai.core.ai.ChatFailure
import dev.openautoai.core.ai.ChatMessage
import dev.openautoai.core.ai.ChatReply
import dev.openautoai.core.ai.ChatRequest
import dev.openautoai.core.storage.CredentialStore
import dev.openautoai.core.ai.ModelCatalog
import dev.openautoai.core.ai.ModelSelection
import dev.openautoai.core.ai.Provider
import dev.openautoai.providers.ProviderChatClient

/** Compatibility bridge from the existing encrypted archive/voice code to the common chat API. */
class ChatApi(private val client: ChatClient) {
    constructor(store: CredentialStore) : this(ProviderChatClient(store))

    fun reply(provider: Provider, modelId: String, turns: List<Turn>): ChatReply {
        if (!ModelCatalog.valid(provider, modelId)) throw ChatFailure(ChatError.INVALID_REQUEST)
        return client.reply(ChatRequest(ModelSelection(provider, modelId),
            turns.map { ChatMessage(it.role, it.content) }))
    }
}
