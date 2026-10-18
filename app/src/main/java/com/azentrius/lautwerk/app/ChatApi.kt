// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app

import com.azentrius.lautwerk.core.ai.ChatClient
import com.azentrius.lautwerk.core.ai.ChatError
import com.azentrius.lautwerk.core.ai.ChatFailure
import com.azentrius.lautwerk.core.ai.ChatMessage
import com.azentrius.lautwerk.core.ai.ChatReply
import com.azentrius.lautwerk.core.ai.ChatRequest
import com.azentrius.lautwerk.core.storage.CredentialStore
import com.azentrius.lautwerk.core.ai.ModelCatalog
import com.azentrius.lautwerk.core.ai.ModelSelection
import com.azentrius.lautwerk.core.ai.Provider
import com.azentrius.lautwerk.providers.ProviderChatClient

/** Compatibility bridge from the existing encrypted archive/voice code to the common chat API. */
class ChatApi(private val client: ChatClient) {
    constructor(store: CredentialStore) : this(ProviderChatClient(store))

    fun reply(provider: Provider, modelId: String, turns: List<Turn>): ChatReply {
        if (!ModelCatalog.valid(provider, modelId)) throw ChatFailure(ChatError.INVALID_REQUEST)
        return client.reply(ChatRequest(ModelSelection(provider, modelId),
            turns.map { ChatMessage(it.role, it.content) }))
    }
}
