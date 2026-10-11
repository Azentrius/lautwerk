package dev.openautoai.prototype

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.ModelSelection
import dev.openautoai.core.ai.Provider
import dev.openautoai.settings.ModelSelectionStore
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModelSelectionStoreTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var store: ModelSelectionStore

    @Before fun prepare() {
        prefs = InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("selection_store_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = ModelSelectionStore(prefs)
    }

    @After fun cleanup() { prefs.edit().clear().commit() }

    @Test fun remembersEachProviderAndNewStoreInstance() {
        assertEquals(ModelSelection.default(), store.load())
        store.select(ModelSelection(Provider.OPENAI, "gpt-5.4"))
        store.selectProvider(Provider.CLAUDE)
        assertEquals(ModelSelection.default(Provider.CLAUDE), store.load())
        store.select(ModelSelection(Provider.CLAUDE, "claude-haiku-4-5"))
        assertEquals("gpt-5.4", store.selectProvider(Provider.OPENAI).modelId)
        val recreated = ModelSelectionStore(prefs)
        assertEquals(Provider.OPENAI, recreated.load().provider)
        assertEquals("claude-haiku-4-5", recreated.selectProvider(Provider.CLAUDE).modelId)
    }

    @Test fun malformedAndCrossProviderPreferencesFallBackWithoutOverwritingData() {
        prefs.edit().putString("provider", "CLAUDE").putString("model.anthropic", "gpt-5.4").commit()
        assertEquals(ModelSelection.default(Provider.CLAUDE), store.load())
        assertEquals("gpt-5.4", prefs.getString("model.anthropic", null))
        prefs.edit().putInt("provider", 99).putInt("model.openai", 99).commit()
        assertEquals(ModelSelection.default(), store.load())
        prefs.edit().putString("provider", "FUTURE_PROVIDER").putString("model.openai", "removed").commit()
        assertEquals(ModelSelection.default(), store.load())
    }

    @Test fun observersSeeAtomicPairsAndStopAfterClose() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val seen = mutableListOf<ModelSelection>()
            val subscription = store.observe { seen += it }
            val otherInstance = ModelSelectionStore(prefs)
            val next = ModelSelection(Provider.CLAUDE, "claude-opus-4-6")
            otherInstance.select(next)
            otherInstance.select(next)
            assertEquals(listOf(ModelSelection.default(), next), seen)
            subscription.close()
            otherInstance.selectProvider(Provider.OPENAI)
            assertEquals(2, seen.size)
        }
    }
}
