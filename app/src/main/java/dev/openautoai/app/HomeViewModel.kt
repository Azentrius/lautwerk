// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import dev.openautoai.core.i18n.Messages
import dev.openautoai.core.i18n.AppLanguage
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

internal enum class HomeOperation(private val text: Messages) {
    START(Messages.OPENING), SAVE_KEY(Messages.KEY_SAVING),
    CLEAR_KEY(Messages.KEY_CLEARING), DELETE_CHAT(Messages.CHAT_DELETING),
    PREPARE_EXPORT(Messages.EXPORT_PREPARING), PICK_EXPORT(Messages.EXPORT_PICKING),
    WRITE_EXPORT(Messages.EXPORT_WRITING);
    val label: String get() = text.text()
}

internal data class HistoryFocus(val chatId: String, val turnIndex: Int)

internal data class HomeState(
    val selection: ModelSelection,
    val sessions: List<Conversation> = emptyList(),
    val selectedId: String? = null,
    val loading: Boolean = true,
    val archiveError: String? = null,
    val operation: HomeOperation? = null,
    val message: String? = null,
    val readyStart: Conversation? = null,
    val exportPickerPending: Boolean = false,
    val exportPresentation: ExportPresentation = ExportPresentation.NONE,
    val historyQuery: String = "",
    val historyProvider: Provider? = null,
    val historyMatches: List<ConversationMatch> = emptyList(),
    val historySearching: Boolean = true,
    val historyError: String? = null,
    val historyFocus: HistoryFocus? = null
) {
    val selected: Conversation? get() = sessions.find { it.id == selectedId }
}

/** Retained across rotation; only immutable snapshots reach Compose. No keys in state or saved state. */
internal class HomeViewModel(
    private val selections: ModelSelectionStore,
    private val credentials: CredentialStore,
    private val conversations: ConversationRepository,
    private val exporter: ConversationExportWriter,
    private val findHistory: (List<Conversation>, String, Provider?, RequestCancellation) -> List<ConversationMatch> = ConversationSearch::find
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
    private var historyWork: Cancel? = null
    private var historyLaunch: Runnable? = null
    private var historyVersion = 0L
    private val selectionObserver = selections.observe {
        if (state.selection != it) state = state.copy(selection = it, selectedId = null, historyFocus = null, message = null)
    }

    private var language = AppLanguage.locale().language
    init { refresh() }

    fun languageChanged() {
        val current = AppLanguage.locale().language
        if (language == current) return
        language = current
        state = state.copy(message = null, archiveError = null, historyError = null)
        if (!AppState.active) {
            AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, Messages.READY.text())
            AppState.reportIssue(null)
        }
        refresh()
    }

    private fun editable() = !closed && !AppState.active && state.operation == null && state.readyStart == null

    fun selectProvider(provider: Provider) {
        if (!editable()) return
        selections.selectProvider(provider)
        state = state.copy(selectedId = null, historyFocus = null, message = null)
    }

    fun selectModel(selection: ModelSelection) {
        if (!editable()) return
        selections.select(selection)
        state = state.copy(selectedId = null, historyFocus = null, message = null)
    }

    fun openChat(id: String) {
        if (!editable() || state.loading || state.archiveError != null) return
        val chat = state.sessions.find { it.id == id } ?: return
        val valid = ModelCatalog.valid(chat.provider, chat.modelId)
        if (valid) selections.select(ModelSelection(chat.provider, chat.modelId))
        state = state.copy(selectedId = id, historyFocus = null, message = if (valid) null else
            Messages.RETIRED_MODEL.text())
    }

    fun newChat() { if (editable()) state = state.copy(selectedId = null, historyFocus = null, message = null) }

    /** Coalesce refresh bursts, and discard a snapshot invalidated by a newer write/revision. */
    fun refresh() {
        if (closed) return
        readVersion++
        reloadWanted = true
        invalidateHistory()
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
                    archiveError = error.storageMessage(Messages.ARCHIVE_FAILED.text())) })
            }
            if (!state.loading) searchHistory()
            loadIfNeeded()
        }
    }

    fun setHistoryQuery(query: String) {
        if (closed) return
        val bounded = query.take(ConversationSearch.MAX_QUERY_LENGTH)
        if (bounded == state.historyQuery) return
        state = state.copy(historyQuery = bounded)
        searchHistory(debounce = true)
    }

    fun setHistoryProvider(provider: Provider?) {
        if (closed || provider == state.historyProvider) return
        state = state.copy(historyProvider = provider)
        searchHistory()
    }

    fun retryHistorySearch() { if (!closed) searchHistory() }

    private fun invalidateHistory() {
        historyVersion++
        historyWork?.cancel(); historyWork = null
        historyLaunch?.let(main::removeCallbacks); historyLaunch = null
        state = state.copy(historyMatches = emptyList(), historySearching = true, historyError = null)
    }

    private fun searchHistory(debounce: Boolean = false) {
        invalidateHistory()
        if (closed || state.loading || state.archiveError != null) {
            state = state.copy(historySearching = !closed && state.loading)
            return
        }
        val token = historyVersion
        val snapshot = state.sessions
        val query = state.historyQuery
        val provider = state.historyProvider
        val launch = Runnable {
            historyLaunch = null
            if (closed || token != historyVersion) return@Runnable
            historyWork = tasks.run({ cancel -> findHistory(snapshot, query, provider, cancel) }) { result ->
                if (closed || token != historyVersion) return@run
                historyWork = null
                state = state.copy(historySearching = false, historyMatches = result.getOrDefault(emptyList()),
                    historyError = if (result.isFailure) Messages.SEARCH_FAILED.text() else null)
            }
        }
        historyLaunch = launch
        main.postDelayed(launch, if (debounce) 200L else 0L)
    }

    fun openHistoryMatch(id: String): Boolean {
        if (!editable() || state.loading || state.archiveError != null || state.historySearching || state.historyError != null) return false
        val match = state.historyMatches.find { it.conversation.id == id } ?: return false
        openChat(id)
        if (state.selectedId != id) return false
        state = state.copy(historyFocus = match.firstMatchingTurn?.let { HistoryFocus(id, it) })
        return true
    }

    fun finishHistoryFocus(expected: HistoryFocus) {
        if (state.historyFocus == expected) state = state.copy(historyFocus = null)
    }

    private fun <T> operate(kind: HomeOperation, work: (RequestCancellation) -> T, success: (T) -> Unit): Boolean {
        if (!editable()) return false
        val token = ++operationVersion
        readVersion++ // A read started before this mutation must not overwrite its result.
        reloadWanted = true
        state = state.copy(operation = kind, message = null, exportPresentation = when(kind) {
            HomeOperation.START -> ExportPresentation.NONE
            HomeOperation.PREPARE_EXPORT -> ExportPresentation.PREPARING
            else -> state.exportPresentation
        })
        mutation = tasks.run(work) { result ->
            mutation = null
            if (closed || token != operationVersion) return@run
            state = state.copy(operation = null)
            result.fold(success) { error ->
                if(kind == HomeOperation.PREPARE_EXPORT) state = state.copy(exportPresentation = ExportPresentation.FAILED)
                state = state.copy(message = error.storageMessage(when (kind) {
                    HomeOperation.START -> Messages.OPEN_FAILED.text()
                    HomeOperation.SAVE_KEY -> Messages.KEY_SAVE_FAILED.text()
                    HomeOperation.CLEAR_KEY -> Messages.KEY_CLEAR_FAILED.text()
                    HomeOperation.DELETE_CHAT -> Messages.DELETE_FAILED.text()
                    HomeOperation.PREPARE_EXPORT -> Messages.EXPORT_PREPARE_FAILED.text()
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
            state = state.copy(message = Messages.KEY_SAVED.text(provider.displayName))
        }
    }

    fun clearKey(): Boolean {
        val provider = state.selection.provider
        return operate(HomeOperation.CLEAR_KEY, { it.check(); credentials.clear(provider) }) {
            state = state.copy(message = Messages.KEY_CLEARED.text(provider.displayName))
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
        state = state.copy(operation = null, readyStart = null, message = Messages.START_CANCELLED.text())
        refresh()
    }

    fun deleteSelected(expectedId: String? = state.selectedId): Boolean {
        val id = expectedId ?: return false
        if (id != state.selectedId) return false
        if (state.loading || state.archiveError != null) return false
        return operate(HomeOperation.DELETE_CHAT, { it.check(); conversations.delete(id) }) {
            state = state.copy(selectedId = null, historyFocus = null, message = Messages.DELETED.text())
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
        state = state.copy(operation = null, exportPickerPending = false, message = Messages.EXPORT_CANCELLED.text(), exportPresentation = ExportPresentation.CANCELLED)
        refresh()
    }

    fun exportPickerFailed() {
        if (closed || state.operation != HomeOperation.PICK_EXPORT) return
        exportSnapshot = null
        state = state.copy(operation = null, exportPickerPending = false,
            message = Messages.EXPORT_PICK_FAILED.text(), exportPresentation = ExportPresentation.FAILED)
        refresh()
    }

    fun finishExportSelection(destination: Uri?) {
        if (closed) return
        val chat = exportSnapshot
        if (state.operation != HomeOperation.PICK_EXPORT || chat == null) {
            // E.g. process death while the picker was open. Never guess which chat to disclose.
            if (destination != null && state.operation == null) state = state.copy(message = EXPORT_INTERRUPTED, exportPresentation = ExportPresentation.FAILED)
            return
        }
        exportSnapshot = null
        state = state.copy(exportPickerPending = false)
        if (destination == null) {
            state = state.copy(operation = null, message = Messages.EXPORT_CANCELLED.text(), exportPresentation = ExportPresentation.CANCELLED)
            refresh()
            return
        }
        val token = ++operationVersion
        state = state.copy(operation = HomeOperation.WRITE_EXPORT, message = null, exportPresentation = ExportPresentation.WRITING)
        mutation = tasks.run({ cancel -> exporter.write(destination, chat, cancel) }) { result ->
            mutation = null
            if (closed || token != operationVersion) return@run
            state = state.copy(operation = null, message = if (result.isSuccess)
                Messages.EXPORTED.text() else EXPORT_FAILED, exportPresentation = if(result.isSuccess) ExportPresentation.DONE else ExportPresentation.FAILED)
            refresh()
        }
    }

    companion object {
        val EXPORT_FAILED get() = Messages.EXPORT_FAILED.text()
        val EXPORT_INTERRUPTED get() = Messages.EXPORT_INTERRUPTED.text()
    }

    override fun onCleared() {
        closed = true
        exportSnapshot = null
        operationVersion++
        readVersion++
        selectionObserver.close()
        load?.cancel(); mutation?.cancel(); historyWork?.cancel()
        historyVersion++
        historyLaunch?.let(main::removeCallbacks)
        worker.shutdownNow()
        main.removeCallbacksAndMessages(null)
    }
}

private fun Throwable.storageMessage(fallback: String) = when (this) {
    is StorageFailure -> message ?: fallback
    is ChatFailure -> message ?: fallback
    else -> fallback
}
