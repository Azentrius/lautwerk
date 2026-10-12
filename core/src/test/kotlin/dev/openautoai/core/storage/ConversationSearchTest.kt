package dev.openautoai.core.storage

import dev.openautoai.core.ai.*
import org.junit.Assert.*
import org.junit.Test

class ConversationSearchTest {
    private fun chat(id: String, provider: Provider = Provider.OPENAI, title: String = "Chat $id",
                     turns: List<Turn> = emptyList()) = Conversation(id, title, provider, "retired-model", 0, turns)

    @Test fun emptyQueryPreservesArchiveOrderAndProviderFilterDoesNotChangeData() {
        val chats = listOf(chat("new", Provider.CLAUDE), chat("old"))
        assertEquals(chats, ConversationSearch.find(chats, "  ", null).map { it.conversation })
        assertEquals(listOf(chats[0]), ConversationSearch.find(chats, "", Provider.CLAUDE).map { it.conversation })
        assertEquals(listOf(chats[1]), ConversationSearch.find(chats, "", Provider.OPENAI).map { it.conversation })
        assertEquals(2, chats.size)
    }

    @Test fun caseInsensitiveUnicodeAndLiteralSubstringMatchTitleModelAndProvider() {
        val c = chat("a", Provider.CLAUDE, "Grüße 🦊 [.*]")
        for (term in listOf("GRÜßE", "🦊", "[.*]", "claude", "ANTHROPIC", "retired")) {
            val result = ConversationSearch.find(listOf(c), " $term ", null).single()
            assertEquals(c, result.conversation); assertNull(result.firstMatchingTurn)
        }
        assertTrue(ConversationSearch.find(listOf(c), ".+", null).isEmpty())
    }

    @Test fun searchFindsOldUserAndAssistantTurnsBeyondUiWindowAndReturnsFirstHit() {
        val turns = (0..149).map { Turn(if (it % 2 == 0) "user" else "assistant", when (it) {
            2, 100 -> "Unverwechselbarer Treffer"; 7 -> "Zweiter Inhalt"; else -> "Nachricht $it"
        }) }
        val c = chat("a", turns = turns)
        assertEquals(2, ConversationSearch.find(listOf(c), "treffer", null).single().firstMatchingTurn)
        assertEquals(7, ConversationSearch.find(listOf(c), "zweiter", null).single().firstMatchingTurn)
        assertEquals(150, c.turns.size)
    }

    @Test fun queryAndProviderAreCombinedAndDoNotSearchInternalIds() {
        val a = chat("private-id", title = "Urlaub"); val b = a.copy(id = "b", provider = Provider.CLAUDE)
        assertEquals(listOf(b), ConversationSearch.find(listOf(a,b), "urlaub", Provider.CLAUDE).map { it.conversation })
        assertTrue(ConversationSearch.find(listOf(a,b), "private-id", null).isEmpty())
        assertTrue(ConversationSearch.find(listOf(a,b), "nicht vorhanden", null).isEmpty())
    }

    @Test fun longMessageMatchCrossingChunkBoundaryIsNotLost() {
        val c = chat("a", turns = listOf(Turn("assistant", "x".repeat(4093) + "Ziel🌍punkt" + "y".repeat(10_000))))
        assertEquals(0, ConversationSearch.find(listOf(c), "Ziel🌍punkt", null).single().firstMatchingTurn)
    }

    @Test fun cancellationWorksBeforeAndInsideOneVeryLongMessage() {
        val c = chat("a", turns = listOf(Turn("user", "x".repeat(200_000))))
        val cancelled = CancellationSource().apply { cancel() }
        assertThrows(ChatFailure::class.java) { ConversationSearch.find(listOf(c), "never", null, cancelled) }
        var checks = 0
        assertThrows(ChatFailure::class.java) {
            ConversationSearch.find(listOf(c), "never", null, RequestCancellation {
                if (++checks > 15) throw ChatFailure(ChatError.CANCELLED)
            })
        }
        assertEquals(16, checks)
    }

    @Test fun queryLengthIsBoundedAndLargeArchiveReturnsEveryMatchingChat() {
        val chats = (0..999).map { chat(it.toString(), title = "a".repeat(200)) }
        assertEquals(1000, ConversationSearch.find(chats, "a".repeat(200) + "ignored", null).size)
        assertEquals(1000, chats.size)
    }
}
