// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import de.azentrius.lautwerk.core.ai.*
import de.azentrius.lautwerk.core.storage.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class HistorySearchTest {
    private val f = HomeTestFixture()
    private val a = Conversation("a", "Erfundener Urlaub", Provider.OPENAI, ModelCatalog.default(Provider.OPENAI), 0,
        listOf(Turn("user", "Besonderer Inhalt")))
    private val b = a.copy(id = "b", title = "Erfundene Reise", provider = Provider.CLAUDE, modelId = "retired-model")
    @After fun cleanup() { f.close() }
    private fun ready() = f.await { !f.home.state.loading && !f.home.state.historySearching }
    private fun create() { f.archive.items = listOf(a,b); f.create(); ready() }

    @Test fun providerFilterAndQueryAreReadOnlyAndOpeningHitKeepsChatAssignment() {
        create()
        val reads = f.archive.reads.get()
        f.main { f.home.setHistoryQuery("INHALT"); f.home.setHistoryProvider(Provider.CLAUDE) }; ready()
        assertEquals(listOf(b), f.home.state.historyMatches.map { it.conversation })
        f.main {
            assertTrue(f.home.openHistoryMatch("b"))
            assertEquals("b", f.home.state.selectedId)
            assertEquals(HistoryFocus("b",0), f.home.state.historyFocus)
            assertFalse(f.home.prepareStart()) // Retired models remain readable only.
            f.home.finishHistoryFocus(HistoryFocus("b",0))
        }
        assertNull(f.home.state.historyFocus)
        assertEquals(reads,f.archive.reads.get()); assertEquals(0,f.keys.reads.get())
        assertEquals(0,f.archive.creates.get()); assertEquals(0,f.archive.deletes.get())
        assertEquals(listOf(a,b),f.archive.items)
    }

    @Test fun delayedOldSearchCannotReplaceNewQueryAndDebounceKeepsLatestInput() {
        val gate = HomeTestFixture.Gate()
        f.findHistory = { chats, query, provider, cancel ->
            if (query == "Urlaub") gate.block()
            // Deliberately ignore cancellation inside the fake computation to exercise callback suppression.
            ConversationSearch.find(chats,query,provider,RequestCancellation {})
        }
        try {
            create(); f.main { f.home.setHistoryQuery("Urlaub") }
            assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            f.main {
                f.home.setHistoryQuery("Re"); f.home.setHistoryQuery("Rei"); f.home.setHistoryQuery("Reise")
                assertTrue(f.home.state.historyMatches.isEmpty()); assertFalse(f.home.openHistoryMatch("a"))
            }
            gate.open(); ready()
            assertEquals(listOf("b"), f.home.state.historyMatches.map { it.conversation.id })
            assertEquals("Reise", f.home.state.historyQuery)
        } finally { gate.open() }
    }

    @Test fun archiveRefreshInvalidatesInFlightSearchAndRemovedChatCannotReappear() {
        val gate = HomeTestFixture.Gate()
        f.findHistory = { chats,query,provider,cancel ->
            if (query == "Inhalt" && chats.size == 2) gate.block()
            ConversationSearch.find(chats,query,provider,RequestCancellation {})
        }
        try {
            create(); f.main { f.home.setHistoryQuery("Inhalt") }
            assertTrue(gate.entered.await(3,TimeUnit.SECONDS))
            f.archive.items = listOf(b)
            f.main { f.home.refresh(); assertFalse(f.home.openHistoryMatch("a")) }
            gate.open(); ready()
            assertEquals(listOf("b"), f.home.state.historyMatches.map { it.conversation.id })
            f.main { assertFalse(f.home.openHistoryMatch("a")) }
        } finally { gate.open() }
    }

    @Test fun unreadableArchiveClearsResultsAndRecoveryRetainsFilter() {
        create(); f.main { f.home.setHistoryProvider(Provider.CLAUDE) }; ready()
        f.archive.failRead = true; f.main { f.home.refresh() }; ready()
        assertNotNull(f.home.state.archiveError); assertTrue(f.home.state.historyMatches.isEmpty())
        f.main { assertFalse(f.home.openHistoryMatch("b")) }
        f.archive.failRead = false; f.main { f.home.refresh() }; ready()
        assertEquals(Provider.CLAUDE,f.home.state.historyProvider)
        assertEquals(listOf("b"),f.home.state.historyMatches.map { it.conversation.id })
    }

    @Test fun activeVoiceAllowsReadOnlySearchButNotOpeningAnotherChat() {
        create()
        f.main {
            AppState.update(true,a.provider,a.modelId,a.id,"Test")
            f.home.setHistoryProvider(Provider.CLAUDE); f.home.setHistoryQuery("Reise")
        }; ready()
        assertEquals(listOf("b"), f.home.state.historyMatches.map { it.conversation.id })
        f.main { assertFalse(f.home.openHistoryMatch("b")) }
        assertTrue(AppState.active); assertEquals("a",AppState.sessionId)
        assertEquals(0,f.keys.reads.get())
    }

    @Test fun failureIsSafeAndRetryRecoversWithoutLeakingQueryOrChatText() {
        var fail = true
        f.findHistory = { chats,query,provider,cancel ->
            if (fail) throw IllegalStateException("private-query-and-content")
            ConversationSearch.find(chats,query,provider,cancel)
        }
        create(); assertEquals("Der Verlauf konnte nicht durchsucht werden. Bitte erneut versuchen.",f.home.state.historyError)
        f.main { assertFalse(f.home.openHistoryMatch("a")) }
        fail = false; f.main { f.home.retryHistorySearch() }; ready()
        assertNull(f.home.state.historyError); assertEquals(2,f.home.state.historyMatches.size)
    }

    @Test fun closingOwnerRejectsNewQueriesAndLateResults() {
        val gate = HomeTestFixture.Gate()
        f.findHistory = { chats,query,provider,cancel ->
            if (query == "Urlaub") gate.block()
            ConversationSearch.find(chats,query,provider,RequestCancellation {})
        }
        try {
            create(); f.main { f.home.setHistoryQuery("Urlaub") }
            assertTrue(gate.entered.await(3,TimeUnit.SECONDS))
            f.close(); gate.open()
            f.main { f.home.setHistoryQuery("Reise"); f.home.setHistoryProvider(Provider.CLAUDE) }
            assertEquals("Urlaub",f.home.state.historyQuery); assertNull(f.home.state.historyProvider)
            assertTrue(f.home.state.historyMatches.isEmpty())
        } finally { gate.open() }
    }
}
