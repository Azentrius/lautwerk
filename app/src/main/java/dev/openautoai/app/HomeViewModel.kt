package dev.openautoai.app

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.openautoai.app.voice.AndroidVoiceTasks
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import dev.openautoai.settings.ModelSelectionStore
import dev.openautoai.voice.Cancel
import java.util.concurrent.Executors

internal enum class HomeOperation(val label: String) {
    START("Gespräch wird geöffnet …"), SAVE_KEY("Schlüssel wird gespeichert …"),
    CLEAR_KEY("Schlüssel wird entfernt …"), DELETE_CHAT("Gespräch wird gelöscht …"),
    PREPARE_EXPORT("Export wird vorbereitet …"), PICK_EXPORT("Speicherort auswählen …"),
    WRITE_EXPORT("Textdatei wird gespeichert …")
}

internal data class HomeState(
    val selection: ModelSelection,
    val sessions: List<Conversation> = emptyList(),
    val selectedId: String? = null,
    val loading: Boolean = true,
    val archiveError: String? = null,
    val operation: HomeOperation? = null,
    val message: String? = null,
    val readyStart: Conversation? = null,
    val exportPickerPending: Boolean = false
) {
    val selected: Conversation? get() = sessions.find { it.id == selectedId }
}

/** Retained across rotation; only immutable snapshots reach Compose. No keys in state or saved state. */
internal class HomeViewModel(
    private val selections: ModelSelectionStore,
    private val credentials: CredentialStore,
    private val conversations: ConversationRepository,
    private val exporter: ConversationExportWriter
) : ViewModel() {
    var state by mutableStateOf(HomeState(selections.load()))
        private set
    private val main = Handler(Looper.getMainLooper())
    // Serialize UI reads/mutations; the repository also locks against the separate voice worker.
    private val worker = Executors.newSingleThreadExecutor()
    private val tasks = AndroidVoiceTasks(worker, main)
    private var load: Cancel? = null
    private var mutation: Cancel? = null
    private var readVersion = 0L
    private var operationVersion = 0L
    private var reloadWanted = false
    private var closed = false
    private var exportSnapshot: Conversation? = null
    private val selectionObserver = selections.observe {
        if (state.selection != it) state = state.copy(selection = it, selectedId = null, message = null)
    }

    init { refresh() }

    private fun editable() = !closed && !AppState.active && state.operation == null && state.readyStart == null

    fun selectProvider(provider: Provider) {
        if (!editable()) return
        selections.selectProvider(provider)
        state = state.copy(selectedId = null, message = null)
    }

    fun selectModel(selection: ModelSelection) {
        if (!editable()) return
        selections.select(selection)
        state = state.copy(selectedId = null, message = null)
    }

    fun openChat(id: String) {
        if (!editable() || state.loading || state.archiveError != null) return
        val chat = state.sessions.find { it.id == id } ?: return
        val valid = ModelCatalog.valid(chat.provider, chat.modelId)
        if (valid) selections.select(ModelSelection(chat.provider, chat.modelId))
        state = state.copy(selectedId = id, message = if (valid) null else
            "Das Modell dieses Chats ist nicht mehr im Katalog. Der Verlauf bleibt erhalten.")
    }

    fun newChat() { if (editable()) state = state.copy(selectedId = null, message = null) }

    /** Coalesce refresh bursts, and discard a snapshot invalidated by a newer write/revision. */
    fun refresh() {
        if (closed) return
        readVersion++
        reloadWanted = true
        loadIfNeeded()
    }

    private fun loadIfNeeded() {
        if (closed || load != null || state.operation != null || !reloadWanted) return
        reloadWanted = false
        val token = readVersion
        state = state.copy(loading = true)
        load = tasks.run({ it.check(); conversations.all() }) { result ->
            load = null
            if (closed) return@run
            if (token == readVersion) {
                state = result.fold({ chats ->
                    state.copy(sessions = chats, loading = false, archiveError = null,
                        selectedId = state.selectedId?.takeIf { id -> chats.any { it.id == id } })
                }, { error -> state.copy(loading = false,
                    archiveError = error.storageMessage("Der lokale Chatverlauf konnte nicht gelesen werden.")) })
            }
            loadIfNeeded()
        }
    }

    private fun <T> operate(kind: HomeOperation, work: (RequestCancellation) -> T, success: (T) -> Unit): Boolean {
        if (!editable()) return false
        val token = ++operationVersion
        readVersion++ // A read started before this mutation must not overwrite its result.
        reloadWanted = true
        state = state.copy(operation = kind, message = null)
        mutation = tasks.run(work) { result ->
            mutation = null
            if (closed || token != operationVersion) return@run
            state = state.copy(operation = null)
            result.fold(success) { error ->
                state = state.copy(message = error.storageMessage(when (kind) {
                    HomeOperation.START -> "Gespräch konnte nicht geöffnet werden."
                    HomeOperation.SAVE_KEY -> "Schlüssel konnte nicht gespeichert werden."
                    HomeOperation.CLEAR_KEY -> "Schlüssel konnte nicht entfernt werden."
                    HomeOperation.DELETE_CHAT -> "Gespräch konnte nicht gelöscht werden."
                    HomeOperation.PREPARE_EXPORT -> "Export konnte nicht vorbereitet werden."
                    HomeOperation.PICK_EXPORT, HomeOperation.WRITE_EXPORT -> EXPORT_FAILED
                }))
            }
            refresh()
        }
        return true
    }

    fun saveKey(secret: String): Boolean {
        if (secret.isBlank()) return false
        val provider = state.selection.provider
        return operate(HomeOperation.SAVE_KEY, { it.check(); credentials.save(provider, secret) }) {
            state = state.copy(message = "Schlüssel für ${provider.displayName} verschlüsselt gespeichert")
        }
    }

    fun clearKey(): Boolean {
        val provider = state.selection.provider
        return operate(HomeOperation.CLEAR_KEY, { it.check(); credentials.clear(provider) }) {
            state = state.copy(message = "Schlüssel für ${provider.displayName} entfernt")
        }
    }

    fun prepareStart(): Boolean {
        if (state.loading || state.archiveError != null ||
            state.selected?.let { !ModelCatalog.valid(it.provider, it.modelId) } == true) return false
        val selection = state.selection
        val id = state.selectedId
        return operate(HomeOperation.START, { cancel ->
            cancel.check()
            if (credentials.read(selection.provider).isNullOrBlank()) throw ChatFailure(ChatError.MISSING_KEY)
            cancel.check()
            val chat = if (id == null) conversations.create(selection.provider, selection.modelId)
                else conversations.get(id) ?: throw StorageFailure(StorageError.CONVERSATION_MISSING)
            if (chat.provider != selection.provider || chat.modelId != selection.modelId)
                throw ChatFailure(ChatError.INVALID_REQUEST)
            cancel.check()
            chat
        }) { chat ->
            if (!AppState.active) state = state.copy(selectedId = chat.id, readyStart = chat)
        }
    }

    /** One-shot handoff to the currently resumed Activity, never a retained Activity callback. */
    fun takeReadyStart(): Conversation? {
        val result = state.readyStart
        state = state.copy(readyStart = null)
        return result
    }

    fun cancelStart() {
        if (state.operation != HomeOperation.START && state.readyStart == null) return
        operationVersion++
        mutation?.cancel(); mutation = null
        state = state.copy(operation = null, readyStart = null, message = "Start abgebrochen")
        refresh()
    }

    fun deleteSelected(expectedId: String? = state.selectedId): Boolean {
        val id = expectedId ?: return false
        if (id != state.selectedId) return false
        if (state.loading || state.archiveError != null) return false
        return operate(HomeOperation.DELETE_CHAT, { it.check(); conversations.delete(id) }) {
            state = state.copy(selectedId = null, message = "Gespräch gelöscht")
        }
    }

    /** Confirmation is tied to the displayed chat, including retired model IDs. Read only. */
    fun prepareExport(expectedId: String?): Boolean {
        val selected = state.selected ?: return false
        if (selected.id != expectedId || state.loading || state.archiveError != null) return false
        return operate(HomeOperation.PREPARE_EXPORT, { cancel ->
            cancel.check()
            val chat = conversations.get(selected.id) ?: throw StorageFailure(StorageError.CONVERSATION_MISSING)
            if (chat.id != selected.id || chat.provider != selected.provider || chat.modelId != selected.modelId)
                throw ChatFailure(ChatError.INVALID_REQUEST)
            cancel.check()
            chat.copy(turns = chat.turns.toList())
        }) { chat ->
            exportSnapshot = chat
            state = state.copy(operation = HomeOperation.PICK_EXPORT, exportPickerPending = true)
        }
    }

    /** Consumed once, so recomposition/rotation never launches a second picker. */
    fun takeExportFileName(): String? {
        if (closed || state.operation != HomeOperation.PICK_EXPORT || !state.exportPickerPending) return null
        state = state.copy(exportPickerPending = false)
        return ConversationTextExport.FILE_NAME
    }

    fun cancelExportPreparation() {
        if (state.operation != HomeOperation.PREPARE_EXPORT) return
        operationVersion++
        mutation?.cancel(); mutation = null
        exportSnapshot = null
        state = state.copy(operation = null, exportPickerPending = false, message = "Export abgebrochen")
        refresh()
    }

    fun exportPickerFailed() {
        if (closed || state.operation != HomeOperation.PICK_EXPORT) return
        exportSnapshot = null
        state = state.copy(operation = null, exportPickerPending = false,
            message = "Speicherauswahl konnte nicht geöffnet werden. Bitte erneut versuchen.")
        refresh()
    }

    fun finishExportSelection(destination: Uri?) {
        if (closed) return
        val chat = exportSnapshot
        if (state.operation != HomeOperation.PICK_EXPORT || chat == null) {
            // E.g. process death while the picker was open. Never guess which chat to disclose.
            if (destination != null && state.operation == null) state = state.copy(message = EXPORT_INTERRUPTED)
            return
        }
        exportSnapshot = null
        state = state.copy(exportPickerPending = false)
        if (destination == null) {
            state = state.copy(operation = null, message = "Export abgebrochen")
            refresh()
            return
        }
        val token = ++operationVersion
        state = state.copy(operation = HomeOperation.WRITE_EXPORT, message = null)
        mutation = tasks.run({ cancel -> exporter.write(destination, chat, cancel) }) { result ->
            mutation = null
            if (closed || token != operationVersion) return@run
            state = state.copy(operation = null, message = if (result.isSuccess)
                "Textdatei gespeichert. Die exportierte Kopie ist unverschlüsselt." else EXPORT_FAILED)
            refresh()
        }
    }

    companion object {
        const val EXPORT_FAILED = "Export fehlgeschlagen. Am gewählten Speicherort kann eine leere oder unvollständige Datei liegen. Bitte dort prüfen oder löschen."
        const val EXPORT_INTERRUPTED = "Export wurde unterbrochen. Bitte erneut exportieren und eine eventuell leere Datei am gewählten Speicherort prüfen oder löschen."
    }

    override fun onCleared() {
        closed = true
        exportSnapshot = null
        operationVersion++
        readVersion++
        selectionObserver.close()
        load?.cancel(); mutation?.cancel()
        worker.shutdownNow()
        main.removeCallbacksAndMessages(null)
    }
}

private fun Throwable.storageMessage(fallback: String) = when (this) {
    is StorageFailure -> message ?: fallback
    is ChatFailure -> message ?: fallback
    else -> fallback
}
