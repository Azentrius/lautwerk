// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.voice

import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import dev.openautoai.providers.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ConversationVoiceBackendTest {
    private class Tasks : VoiceTasks {
        var cancelled = false
        override fun <T> run(work: (CancellationCheck) -> T, complete: (Result<T>) -> Unit): Cancel {
            complete(runCatching { work(CancellationCheck { if (cancelled) throw ChatFailure(ChatError.CANCELLED) }) })
            return Cancel { cancelled = true }
        }
    }
    private class Archive : ConversationRepository {
        val chats = mutableMapOf<String, Conversation>()
        var failRole: String? = null
        override fun all() = chats.values.toList()
        override fun get(id: String) = chats[id]
        override fun create(provider: Provider, modelId: String): Conversation {
            val c = Conversation("chat-${chats.size}", "Test", provider, modelId, 0, emptyList())
            chats[c.id] = c; return c
        }
        override fun append(id: String, turn: Turn) {
            if (turn.role == failRole) throw StorageFailure(StorageError.ARCHIVE_WRITE)
            val old = chats[id] ?: throw StorageFailure(StorageError.CONVERSATION_MISSING)
            chats[id] = old.copy(turns = old.turns + turn)
        }
        override fun delete(id: String) { chats.remove(id) }
    }
    private class Keys : CredentialStore {
        val values = Provider.entries.associateWith { "dummy-${it.name}" }.toMutableMap()
        val reads = mutableListOf<Provider>()
        override fun read(provider: Provider): String? { reads += provider; return values[provider] }
        override fun save(provider: Provider, secret: String) { values[provider] = secret }
        override fun clear(provider: Provider) { values.remove(provider) }
    }
    private class Fixture(client: ChatClient) {
        val tasks = Tasks()
        val archive = Archive()
        val keys = Keys()
        var saved: ModelSelection? = null
        var revisions = 0
        val backend = ConversationVoiceBackend(archive, keys, client, tasks,
            { ModelSelection(it, ModelCatalog.default(it)) }, { saved = it }, { revisions++ })
        fun chat(provider: Provider = Provider.OPENAI) = archive.create(provider, ModelCatalog.default(provider))
    }

    @Test fun bothRealAdaptersConnectTextToPersistedReplyWithoutLiveNetwork() {
        for (provider in Provider.entries) {
            val keys = Keys()
            var sent: HttpRequest? = null
            val transport = HttpTransport {
                sent = it
                HttpResponse(200, if (provider == Provider.OPENAI)
                    """{"object":"response","status":"completed","output":[{"type":"message","role":"assistant","content":[{"type":"output_text","text":"Guten Tag!"}]}]}"""
                else """{"type":"message","role":"assistant","stop_reason":"end_turn","content":[{"type":"text","text":"Guten Tag!"}]}""")
            }
            val f = Fixture(ProviderChatClient(keys, transport))
            val c = f.chat(provider)
            var reply: ChatReply? = null
            f.backend.reply(c, "Hallo per Sprache") { reply = it.getOrThrow() }
            assertEquals("Guten Tag!", reply!!.text)
            val field = if (provider == Provider.OPENAI) "input" else "messages"
            assertEquals("Hallo per Sprache", JSONObject(sent!!.body).getJSONArray(field).getJSONObject(0).getString("content"))
            assertEquals(listOf("user", "assistant"), f.archive.get(c.id)!!.turns.map { it.role })
            assertEquals(listOf(provider), keys.reads)
            assertEquals(2, f.revisions)
        }
    }

    @Test fun providerSwitchCreatesEmptyChatAndNeverCopiesOldContext() {
        val requests = mutableListOf<ChatRequest>()
        val f = Fixture(ChatClient { requests += it; ChatReply("Antwort") })
        val old = f.chat()
        f.archive.append(old.id, Turn("user", "Vertraulicher alter Verlauf"))
        var switched: Conversation? = null
        f.backend.switchProvider(Provider.CLAUDE) { switched = it.getOrThrow() }
        assertTrue(switched!!.turns.isEmpty())
        assertEquals(Provider.CLAUDE, f.saved!!.provider)
        f.backend.reply(switched!!, "Neue Frage") { it.getOrThrow() }
        assertEquals(listOf("Neue Frage"), requests.single().messages.map { it.content })
        assertEquals(1, f.archive.get(old.id)!!.turns.size)
    }

    @Test fun missingSwitchCredentialsDoNotChangeSelectionOrCreateConversation() {
        val f = Fixture(ChatClient { error("must not call") })
        f.chat(); f.keys.clear(Provider.CLAUDE)
        var failure: Throwable? = null
        f.backend.switchProvider(Provider.CLAUDE) { failure = it.exceptionOrNull() }
        assertEquals(ChatError.MISSING_KEY, (failure as ChatFailure).kind)
        assertEquals(1, f.archive.all().size)
        assertNull(f.saved)
    }

    @Test fun failedUserWritePreventsApiCallAndAssistantWriteFailureIsReported() {
        var calls = 0
        val f = Fixture(ChatClient { calls++; ChatReply("Antwort") })
        val c = f.chat()
        f.archive.failRole = "user"
        f.backend.reply(c, "Nicht gespeichert") { assertTrue(it.exceptionOrNull() is StorageFailure) }
        assertEquals(0, calls)
        f.archive.failRole = "assistant"
        f.backend.reply(c, "Gespeicherte Frage") { assertTrue(it.exceptionOrNull() is StorageFailure) }
        assertEquals(1, calls)
        assertEquals(listOf("user"), f.archive.get(c.id)!!.turns.map { it.role })
    }

    @Test fun responseAfterCancellationIsNotArchived() {
        lateinit var f: Fixture
        f = Fixture(ChatClient { f.tasks.cancelled = true; ChatReply("Zu spät") })
        val c = f.chat()
        f.backend.reply(c, "Frage") { assertEquals(ChatError.CANCELLED, (it.exceptionOrNull() as ChatFailure).kind) }
        assertEquals(listOf("user"), f.archive.get(c.id)!!.turns.map { it.role })
    }

    @Test fun changedOrDeletedConversationCannotReachProvider() {
        var calls = 0
        val f = Fixture(ChatClient { calls++; ChatReply("Antwort") })
        val c = f.chat()
        f.archive.chats[c.id] = c.copy(provider = Provider.CLAUDE, modelId = ModelCatalog.default(Provider.CLAUDE))
        f.backend.reply(c, "Frage") { assertTrue(it.isFailure) }
        f.archive.delete(c.id)
        f.backend.reply(c, "Frage") { assertTrue(it.isFailure) }
        assertEquals(0, calls)
    }

    @Test fun contextIsBoundedWhileCompleteTranscriptAndRawPartialResponseRemainLocal() {
        var request: ChatRequest? = null
        val f = Fixture(ChatClient { request = it; ChatReply("Teilantwort", Completion.TRUNCATED) })
        val c = f.chat()
        repeat(20) { f.archive.append(c.id, Turn(if (it % 2 == 0) "user" else "assistant", "Nachricht $it")) }
        f.backend.reply(c, "Aktuelle Frage") { assertEquals(Completion.TRUNCATED, it.getOrThrow().completion) }
        assertEquals(12, request!!.messages.size)
        assertEquals("Aktuelle Frage", request!!.messages.last().content)
        assertEquals(22, f.archive.get(c.id)!!.turns.size)
        assertEquals("Teilantwort", f.archive.get(c.id)!!.turns.last().content)
    }

    @Test fun missingAndRetiredConversationCannotStartSpeech() {
        val f = Fixture(ChatClient { error("must not call") })
        f.backend.open("missing") { assertTrue(it.exceptionOrNull() is StorageFailure) }
        val c = f.chat()
        f.archive.chats[c.id] = c.copy(modelId = "retired-model")
        f.backend.open(c.id) { assertEquals(ChatError.INVALID_REQUEST, (it.exceptionOrNull() as ChatFailure).kind) }
    }
}
