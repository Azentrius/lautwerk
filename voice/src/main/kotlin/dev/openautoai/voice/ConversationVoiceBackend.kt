package dev.openautoai.voice

import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*

/** Persistence and API work run through the injected worker, never from speech callbacks directly. */
class ConversationVoiceBackend(
    private val conversations: ConversationRepository,
    private val credentials: CredentialStore,
    private val client: ChatClient,
    private val tasks: VoiceTasks,
    private val selectionFor: (Provider) -> ModelSelection,
    private val saveSelection: (ModelSelection) -> Unit,
    private val changed: () -> Unit
) : VoiceBackend {
    private fun validate(conversation: Conversation) {
        if (!ModelCatalog.valid(conversation.provider, conversation.modelId)) throw ChatFailure(ChatError.INVALID_REQUEST)
        if (credentials.read(conversation.provider).isNullOrBlank()) throw ChatFailure(ChatError.MISSING_KEY)
    }

    override fun open(id: String, complete: (Result<Conversation>) -> Unit): Cancel = tasks.run({ cancel ->
        cancel.check()
        val conversation = conversations.get(id) ?: throw StorageFailure(StorageError.CONVERSATION_MISSING)
        validate(conversation)
        cancel.check()
        conversation
    }, complete)

    override fun switchProvider(provider: Provider, complete: (Result<Conversation>) -> Unit): Cancel = tasks.run({ cancel ->
        cancel.check()
        val selection = selectionFor(provider)
        if (selection.provider != provider) throw ChatFailure(ChatError.INVALID_REQUEST)
        if (credentials.read(provider).isNullOrBlank()) throw ChatFailure(ChatError.MISSING_KEY)
        cancel.check()
        // Never copy the former provider's transcript into this new conversation.
        conversations.create(provider, selection.modelId).also {
            cancel.check()
            saveSelection(selection)
            changed()
        }
    }, complete)

    override fun reply(conversation: Conversation, phrase: String, complete: (Result<ChatReply>) -> Unit): Cancel = tasks.run({ cancel ->
        cancel.check()
        val current = conversations.get(conversation.id) ?: throw StorageFailure(StorageError.CONVERSATION_MISSING)
        if (current.provider != conversation.provider || current.modelId != conversation.modelId)
            throw ChatFailure(ChatError.INVALID_REQUEST)
        validate(current)
        cancel.check()
        val user = Turn("user", phrase)
        conversations.append(current.id, user)
        changed()
        cancel.check()
        val messages = (current.turns + user).takeLast(12).map { ChatMessage(it.role, it.content) }
        val response = client.reply(ChatRequest(ModelSelection(current.provider, current.modelId), messages, cancel))
        cancel.check() // Late responses after Stop are neither spoken nor newly archived.
        if (response.text.isBlank()) throw ChatFailure(ChatError.EMPTY_RESPONSE)
        conversations.append(current.id, Turn("assistant", response.text))
        changed()
        cancel.check()
        response
    }, complete)
}
