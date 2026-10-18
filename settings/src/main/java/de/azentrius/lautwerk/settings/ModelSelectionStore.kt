// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.settings

import android.content.Context
import android.content.SharedPreferences
import de.azentrius.lautwerk.core.ai.ModelCatalog
import de.azentrius.lautwerk.core.ai.ModelSelection
import de.azentrius.lautwerk.core.ai.Provider
import java.io.Closeable

/** Non-secret, app-private preferences. Phone and CarAppService run in the same process. */
class ModelSelectionStore(private val preferences: SharedPreferences) {
    constructor(context: Context) : this(context.applicationContext.getSharedPreferences(
        "model_selection_v1", Context.MODE_PRIVATE
    ))

    fun load(): ModelSelection {
        // Read one snapshot and tolerate unknown names, removed IDs and malformed values.
        val values = preferences.all
        val provider = Provider.entries.find { it.name == values["provider"] } ?: Provider.OPENAI
        return selectionFor(provider, values)
    }

    fun forProvider(provider: Provider): ModelSelection = selectionFor(provider, preferences.all)

    fun selectProvider(provider: Provider): ModelSelection = select(forProvider(provider))

    fun select(selection: ModelSelection): ModelSelection {
        // Both keys become visible together; the previous provider's remembered model stays intact.
        preferences.edit().putString("provider", selection.provider.name)
            .putString(modelKey(selection.provider), selection.modelId).apply()
        return selection
    }

    /** Subscribe on the main thread. Close with the owning UI lifecycle to avoid retaining it. */
    fun observe(onChanged: (ModelSelection) -> Unit): Closeable {
        var previous = load()
        var active = true
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            if (active) {
                val current = load()
                if (current != previous) {
                    previous = current
                    onChanged(current)
                }
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onChanged(previous)
        // The returned handle also keeps a strong reference to Android's weakly-held listener.
        return Closeable {
            active = false
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun selectionFor(provider: Provider, values: Map<String, *>): ModelSelection {
        val stored = values[modelKey(provider)] as? String
        return if (stored != null && ModelCatalog.valid(provider, stored)) ModelSelection(provider, stored)
        else ModelSelection.default(provider)
    }

    private fun modelKey(provider: Provider) = "model.${provider.keyId}"
}
