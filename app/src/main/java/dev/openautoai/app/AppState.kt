package dev.openautoai.app

import dev.openautoai.core.ai.Provider
import dev.openautoai.core.ai.ModelCatalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Single-process observable state. Mutations are dispatched on the main thread. */
object AppState {
    var active by mutableStateOf(false)
        private set
    var provider by mutableStateOf(Provider.OPENAI)
        private set
    var modelId by mutableStateOf(ModelCatalog.default(Provider.OPENAI))
        private set
    var sessionId by mutableStateOf<String?>(null)
        private set
    var status by mutableStateOf("Bereit")
        private set
    var revision by mutableIntStateOf(0)
        private set

    fun update(active: Boolean, provider: Provider, modelId: String, sessionId: String?, status: String) {
        this.active = active
        this.provider = provider
        this.modelId = modelId
        this.sessionId = sessionId
        this.status = status
    }
    fun changed() { revision++ }
}
