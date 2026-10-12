package dev.openautoai.app

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.ModelCatalog
import dev.openautoai.core.ai.Provider
import dev.openautoai.core.storage.*
import dev.openautoai.settings.ModelSelectionStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Injected stores only: no real chat, API key, recording or service is touched. */
@RunWith(AndroidJUnit4::class)
class StorageUiTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "storage-ui-test-${UUID.randomUUID()}"
    private lateinit var selections: ModelSelectionStore
    private var starts = 0

    @Before fun setUp() {
        selections = ModelSelectionStore(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        compose.runOnIdle {
            AppState.update(false, Provider.OPENAI, ModelCatalog.default(Provider.OPENAI), null, "Bereit")
            AppState.reportIssue(null)
        }
    }
    @After fun tearDown() { context.deleteSharedPreferences(name) }

    private class FakeArchive : ConversationRepository {
        var failRead = false
        var items = emptyList<Conversation>()
        var creates = 0
        override fun all(): List<Conversation> {
            if (failRead) throw StorageFailure(StorageError.ARCHIVE_READ)
            return items
        }
        override fun get(id: String) = items.find { it.id == id }
        override fun create(provider: Provider, modelId: String): Conversation {
            creates++
            return Conversation("test", "Test", provider, modelId, 0, emptyList())
        }
        override fun append(id: String, turn: Turn) = Unit
        override fun delete(id: String) = Unit
    }
    private class FakeCredentials : CredentialStore {
        var failRead = false
        override fun read(provider: Provider): String? {
            if (failRead) throw StorageFailure(StorageError.CREDENTIAL_READ)
            return "dummy-key"
        }
        override fun save(provider: Provider, secret: String) = Unit
        override fun clear(provider: Provider) { throw StorageFailure(StorageError.CREDENTIAL_WRITE) }
    }
    private fun render(archive: FakeArchive, keys: FakeCredentials = FakeCredentials()) {
        compose.setContent { MaterialTheme {
            MainContent(selections, keys, archive, { starts++ }, {})
        } }
    }

    @Test fun unreadableArchiveShowsRetryAndDisablesStartUntilRecovery() {
        val archive = FakeArchive().apply { failRead = true }
        render(archive)
        compose.onNodeWithText("Sprachgespräch starten").assertIsNotEnabled()
        compose.onNodeWithText(StorageError.ARCHIVE_READ.userMessage).assertExists()
        compose.runOnIdle { archive.failRead = false }
        compose.onNodeWithText("Verlauf erneut laden").performScrollTo().performClick()
        compose.onNodeWithText(StorageError.ARCHIVE_READ.userMessage).assertDoesNotExist()
        compose.onNodeWithText("Sprachgespräch starten").assertIsEnabled()
        assertEquals(0, starts)
        assertEquals(0, archive.creates)
    }

    @Test fun keyReadAndClearErrorsStayInUiAndDoNotStartVoice() {
        val archive = FakeArchive()
        val keys = FakeCredentials().apply { failRead = true }
        render(archive, keys)
        compose.onNodeWithText("Sprachgespräch starten").performScrollTo().performClick()
        compose.onNodeWithText(StorageError.CREDENTIAL_READ.userMessage).assertExists()
        compose.onNodeWithText("Entfernen").performScrollTo().performClick()
        compose.onNodeWithText(StorageError.CREDENTIAL_WRITE.userMessage).assertExists()
        assertEquals(0, starts)
        assertEquals(0, archive.creates)
    }

    @Test fun retiredModelHistoryRemainsReadableButCannotBeStarted() {
        val archive = FakeArchive().apply {
            items = listOf(Conversation("old", "Älterer Chat", Provider.CLAUDE, "retired-model", 0,
                listOf(Turn("user", "Erhaltene Nachricht"))))
        }
        render(archive)
        compose.onNodeWithText("Älterer Chat · retired-model (1 Nachrichten)").performScrollTo().performClick()
        compose.onNodeWithText("Du: Erhaltene Nachricht").assertExists()
        compose.onNodeWithText("Gespräch fortsetzen").assertIsNotEnabled()
        assertEquals(0, starts)
    }
}
