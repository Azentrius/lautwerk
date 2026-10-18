// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {
    private val f = HomeTestFixture()
    @After fun tearDown() { f.close() }

    @Test fun bothProvidersStartOnceWithSelectedModelAndStoreWorkIsOffMain() {
        f.create(); f.await { !f.home.state.loading }
        for (provider in Provider.entries) {
            f.main { f.home.selectProvider(provider); assertTrue(f.home.saveKey("dummy")) }
            f.await { f.home.state.operation == null && !f.home.state.loading }
            f.main {
                assertTrue(f.home.prepareStart())
                assertFalse(f.home.prepareStart())
            }
            f.await { f.home.state.readyStart != null && !f.home.state.loading }
            f.main {
                val chat = f.home.takeReadyStart()!!
                assertEquals(provider, chat.provider)
                assertEquals(ModelCatalog.default(provider), chat.modelId)
                assertNull(f.home.takeReadyStart())
                assertTrue(f.home.clearKey())
            }
            f.await { f.home.state.operation == null && !f.home.state.loading }
            f.main { assertTrue(f.home.deleteSelected()) }
            f.await { f.home.state.operation == null && !f.home.state.loading }
        }
        assertEquals(2, f.archive.creates.get()); assertEquals(2, f.archive.deletes.get())
        assertEquals(2, f.keys.saves.get()); assertEquals(2, f.keys.clears.get())
    }

    @Test fun refreshBurstsAreCoalescedAndOldSnapshotIsNeverPublished() {
        val gate = HomeTestFixture.Gate()
        val old = Conversation("old", "Old", Provider.OPENAI, ModelCatalog.default(Provider.OPENAI), 0, emptyList())
        f.archive.items = listOf(old); f.archive.beforeAll = gate::block
        try {
            f.create(); assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            f.archive.items = listOf(old.copy(id = "new"))
            f.main { repeat(20) { f.home.refresh() } }
            f.archive.beforeAll = null; gate.open()
            f.await { !f.home.state.loading }
            assertEquals(listOf("new"), f.home.state.sessions.map { it.id })
            assertEquals(2, f.archive.reads.get())
        } finally { gate.open() }
    }

    @Test fun writeDuringSlowReadInvalidatesTheReadAndKeepsProviderCaptured() {
        val gate = HomeTestFixture.Gate(); f.archive.beforeAll = gate::block
        try {
            f.create(); assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            f.main {
                assertTrue(f.home.saveKey("dummy"))
                f.home.selectProvider(Provider.CLAUDE) // Disabled while this write is pending.
                assertEquals(Provider.OPENAI, f.home.state.selection.provider)
            }
            f.archive.items = listOf(Conversation("fresh", "Fresh", Provider.CLAUDE, ModelCatalog.default(Provider.CLAUDE), 0, emptyList()))
            f.archive.beforeAll = null; gate.open()
            f.await { f.home.state.operation == null && !f.home.state.loading }
            assertEquals(listOf("fresh"), f.home.state.sessions.map { it.id })
            assertEquals(Provider.OPENAI, f.keys.savedProvider)
        } finally { gate.open() }
    }

    @Test fun changedSelectedChatCannotSilentlyCreateAnotherChat() {
        val old = Conversation("selected", "Test", Provider.CLAUDE, ModelCatalog.default(Provider.CLAUDE), 0, emptyList())
        f.archive.items = listOf(old); f.create(); f.await { !f.home.state.loading }
        f.main { f.home.openChat(old.id) }
        f.archive.items = listOf(old.copy(provider = Provider.OPENAI, modelId = ModelCatalog.default(Provider.OPENAI)))
        f.main { assertTrue(f.home.prepareStart()) }
        f.await { f.home.state.operation == null && !f.home.state.loading }
        assertEquals(ChatError.INVALID_REQUEST.userMessage, f.home.state.message)
        assertNull(f.home.state.readyStart); assertEquals(0, f.archive.creates.get())
    }

    @Test fun closingScreenOwnerCancelsPendingStartWithoutLateCreation() {
        val gate = HomeTestFixture.Gate(); f.keys.beforeRead = gate::block
        try {
            f.create(); f.await { !f.home.state.loading }
            f.main { f.home.prepareStart() }
            assertTrue(gate.entered.await(3, TimeUnit.SECONDS))
            f.close(); gate.open()
            // The old worker must pass its cancellation check before any archive creation.
            val drained = java.util.concurrent.CountDownLatch(1)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ drained.countDown() }, 300)
            assertTrue(drained.await(3, TimeUnit.SECONDS))
            assertEquals(0, f.archive.creates.get()); assertNull(f.home.state.readyStart)
        } finally { gate.open() }
    }

    @Test fun staleDeletionConfirmationCannotDeleteNewSelection() {
        val a = Conversation("a", "A", Provider.OPENAI, ModelCatalog.default(Provider.OPENAI), 0, emptyList())
        val b = a.copy(id = "b", title = "B")
        f.archive.items = listOf(a, b); f.create(); f.await { !f.home.state.loading }
        f.main {
            f.home.openChat("a")
            f.home.openChat("b")
            assertFalse(f.home.deleteSelected("a"))
        }
        assertEquals(0, f.archive.deletes.get()); assertEquals(listOf(a, b), f.archive.items)
    }

    @Test fun deletedChatReturnsMissingErrorWithoutCreatingReplacement() {
        val chat = Conversation("gone", "Gone", Provider.CLAUDE, ModelCatalog.default(Provider.CLAUDE), 0, emptyList())
        f.archive.items = listOf(chat); f.create(); f.await { !f.home.state.loading }
        f.main { f.home.openChat("gone") }
        f.archive.items = emptyList()
        f.main { assertTrue(f.home.prepareStart()) }
        f.await { f.home.state.operation == null && !f.home.state.loading }
        assertEquals(StorageError.CONVERSATION_MISSING.userMessage, f.home.state.message)
        assertNull(f.home.state.readyStart); assertEquals(0, f.archive.creates.get())
    }
}
