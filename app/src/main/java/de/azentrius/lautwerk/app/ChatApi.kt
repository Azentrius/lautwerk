// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import de.azentrius.lautwerk.core.ai.ChatClient
import de.azentrius.lautwerk.core.ai.ChatError
import de.azentrius.lautwerk.core.ai.ChatFailure
import de.azentrius.lautwerk.core.ai.ChatMessage
import de.azentrius.lautwerk.core.ai.ChatReply
import de.azentrius.lautwerk.core.ai.ChatRequest
import de.azentrius.lautwerk.core.storage.CredentialStore
import de.azentrius.lautwerk.core.ai.ModelCatalog
import de.azentrius.lautwerk.core.ai.ModelSelection
import de.azentrius.lautwerk.core.ai.Provider
import de.azentrius.lautwerk.providers.ProviderChatClient

/** Compatibility bridge from the existing encrypted archive/voice code to the common chat API. */
class ChatApi(private val client: ChatClient) {
    constructor(store: CredentialStore) : this(ProviderChatClient(store))

    fun reply(provider: Provider, modelId: String, turns: List<Turn>): ChatReply {
        if (!ModelCatalog.valid(provider, modelId)) throw ChatFailure(ChatError.INVALID_REQUEST)
        return client.reply(ChatRequest(ModelSelection(provider, modelId),
            turns.map { ChatMessage(it.role, it.content) }))
    }
}
