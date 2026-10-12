package dev.openautoai.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.openautoai.core.ai.*

@Composable
internal fun MainContent(home: HomeViewModel, onStart: (Conversation) -> Unit,
                         onStop: () -> Unit, onSpeechSettings: () -> Unit = {},
                         onExportDocument: (String) -> Unit = {}) {
    val state = home.state
    val active = AppState.active
    val pending = state.operation == HomeOperation.START || state.readyStart != null
    val editable = !active && state.operation == null && state.readyStart == null
    var page by rememberSaveable { mutableStateOf("conversation") }
    // Never save a typed secret in saved-instance state or the ViewModel.
    var keyText by remember(state.selection.provider) { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var exportId by remember { mutableStateOf<String?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val list = rememberLazyListState()
    val shown = if (active) state.sessions.find { it.id == AppState.sessionId } else state.selected
    var visibleTurns by rememberSaveable(shown?.id) { mutableIntStateOf(50) }
    val revision = AppState.revision
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumed by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    var lastPage by rememberSaveable { mutableStateOf<String?>(null) }
    var lastChatId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(revision) { home.refresh() }
    LaunchedEffect(page, shown?.id, state.historyFocus) {
        val changed = lastPage != page || lastChatId != shown?.id
        lastPage = page
        lastChatId = shown?.id
        val focus = state.historyFocus
        if (page == "conversation" && shown != null && focus?.chatId == shown.id && focus.turnIndex in shown.turns.indices) {
            visibleTurns = maxOf(visibleTurns, shown.turns.size - focus.turnIndex)
            withFrameNanos { } // Let the expanded LazyColumn receive its older messages first.
            val first = (shown.turns.size - visibleTurns).coerceAtLeast(0)
            val notices = listOf(state.message, AppState.notice, state.archiveError).count { it != null } + if (state.loading) 1 else 0
            // Provider, settings, explanation, title, export; optionally the older-messages button.
            val index = notices + 5 + (if (first > 0) 1 else 0) + focus.turnIndex - first
            list.scrollToItem(index)
            home.finishHistoryFocus(focus)
        } else if (changed) list.scrollToItem(0)
    }
    LaunchedEffect(state.message, AppState.notice) {
        if (state.historyFocus == null && (state.message != null || AppState.notice != null)) list.scrollToItem(0)
    }
    LaunchedEffect(state.readyStart, resumed) {
        if (resumed) home.takeReadyStart()?.let { keyboard?.hide(); page = "conversation"; onStart(it) }
    }

    LaunchedEffect(state.exportPickerPending, resumed) {
        if (resumed) home.takeExportFileName()?.let { keyboard?.hide(); onExportDocument(it) }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("conversation" to R.string.conversation, "settings" to R.string.settings, "history" to R.string.history).forEach { (name, title) ->
                            FilterChip(selected = page == name, onClick = { keyboard?.hide(); page = name },
                                label = { Text(stringResource(title)) }, modifier = Modifier.testTag("page-$name"))
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.operation?.label ?: AppState.status, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (active) {
                        Button(onClick = onStop, modifier = Modifier.fillMaxWidth().testTag("conversation-stop")) {
                            Text(stringResource(R.string.conversation_stop))
                        }
                    } else if (state.operation == HomeOperation.PREPARE_EXPORT) {
                        OutlinedButton(onClick = home::cancelExportPreparation, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.export_cancel))
                        }
                    } else if (pending) {
                        OutlinedButton(onClick = home::cancelStart, modifier = Modifier.fillMaxWidth().testTag("start-cancel")) {
                            Text(stringResource(R.string.start_cancel))
                        }
                    } else {
                        Button(onClick = { keyboard?.hide(); home.prepareStart() },
                            enabled = editable && !state.loading && state.archiveError == null &&
                                state.selected?.let { ModelCatalog.valid(it.provider, it.modelId) } != false,
                            modifier = Modifier.fillMaxWidth().testTag("conversation-start")) {
                            Text(stringResource(if (state.selectedId != null) R.string.conversation_resume else R.string.conversation_start))
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("home-list"),
            state = list, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.message != null) item("storage-message") { Text(state.message, Modifier.testTag("storage-message")) }
            if (AppState.notice != null) item("voice-notice") { Text(AppState.notice!!, color = MaterialTheme.colorScheme.error) }
            if (state.loading) item("archive-loading") { Text(stringResource(R.string.history_loading)) }
            if (state.archiveError != null) item("archive-error") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.archiveError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = home::refresh, enabled = state.operation == null) { Text(stringResource(R.string.history_reload)) }
                }
            }
            when (page) {
                "conversation" -> {
                    item("selected-provider") {
                        val provider = if (active) AppState.provider else shown?.provider ?: state.selection.provider
                        val model = if (active) AppState.modelId else shown?.modelId ?: state.selection.modelId
                        Text(provider.displayName, style = MaterialTheme.typography.titleLarge)
                        Text(model, style = MaterialTheme.typography.bodyMedium)
                    }
                    item("conversation-settings") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { page = "settings" }, enabled = editable, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.provider_model)) }
                            OutlinedButton(onClick = onSpeechSettings, enabled = editable, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.voice_settings)) }
                        }
                    }
                    item("voice-explanation") {
                        Text(stringResource(R.string.conversation_hint))
                        Text(stringResource(R.string.command_hint), style = MaterialTheme.typography.bodySmall)
                    }
                    if (shown == null) item("empty-chat") {
                        Text(stringResource(R.string.empty_chat))
                    } else {
                        item("chat-title") { Text(stringResource(R.string.opened_chat, shown.title), style = MaterialTheme.typography.titleMedium) }
                        item("export-chat") {
                            OutlinedButton(onClick = { exportId = shown.id },
                                enabled = editable && !state.loading && state.archiveError == null,
                                modifier = Modifier.testTag("export-chat")) { Text(stringResource(R.string.export_chat)) }
                        }
                        if (shown.turns.size > visibleTurns) item("older-turns") {
                            OutlinedButton(onClick = { visibleTurns += 50 }) { Text(stringResource(R.string.older_messages)) }
                        }
                        val first = (shown.turns.size - visibleTurns).coerceAtLeast(0)
                        items(shown.turns.size - first, key = { "turn-${shown.id}-${first + it}" }) { offset ->
                            val turn = shown.turns[first + offset]
                            Text(stringResource(R.string.message_content, stringResource(if (turn.role == "user") R.string.user_role else R.string.assistant_role), turn.content),
                                Modifier.testTag("chat-turn-${first + offset}"))
                        }
                        item("delete-chat") {
                            OutlinedButton(onClick = { deleteId = shown.id }, enabled = editable && !state.loading && state.archiveError == null) {
                                Text(stringResource(R.string.delete_chat))
                            }
                        }
                    }
                    item("voice-limits") {
                        Text(stringResource(R.string.conversation_limits), style = MaterialTheme.typography.bodySmall)
                    }
                }
                "settings" -> {
                    item("providers") {
                        Text(stringResource(R.string.provider_label), style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Provider.entries.forEach { provider ->
                                FilterChip(selected = state.selection.provider == provider,
                                    onClick = { home.selectProvider(provider) }, enabled = editable,
                                    label = { Text(if (provider == Provider.OPENAI) "OpenAI" else "Claude") })
                            }
                        }
                    }
                    item("models-label") { Text(stringResource(R.string.model_access)) }
                    items(ModelCatalog.forProvider(state.selection.provider), key = { "model-${it.id}" }) { model ->
                        FilterChip(selected = state.selection.modelId == model.id,
                            onClick = { home.selectModel(ModelSelection(state.selection.provider, model.id)) },
                            enabled = editable, label = { Text(model.label) })
                    }
                    item("key-label") {
                        Text(stringResource(R.string.credentials), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.key_hint, state.selection.provider.displayName))
                    }
                    item("key-input") {
                        OutlinedTextField(value = keyText, onValueChange = { keyText = it }, enabled = editable,
                            visualTransformation = PasswordVisualTransformation(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("api-key-input"),
                            label = { Text(stringResource(R.string.key_label, state.selection.provider.displayName)) })
                    }
                    item("key-actions") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if (home.saveKey(keyText)) { keyText = ""; keyboard?.hide() } },
                                enabled = editable && keyText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.key_save)) }
                            OutlinedButton(onClick = { if (home.clearKey()) keyText = "" }, enabled = editable) { Text(stringResource(R.string.remove)) }
                        }
                    }
                    item("privacy") {
                        Text(stringResource(R.string.privacy))
                        Text(stringResource(R.string.auto_audio_pending), style = MaterialTheme.typography.bodySmall)
                    }
                }
                "history" -> {
                    item("history-search") {
                        OutlinedTextField(value = state.historyQuery, onValueChange = home::setHistoryQuery,
                            singleLine = true, modifier = Modifier.fillMaxWidth().testTag("history-search"),
                            label = { Text(stringResource(R.string.history_search)) },
                            supportingText = { Text(stringResource(R.string.history_search_hint)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focusManager.clearFocus() }))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf<Provider?>(null, Provider.OPENAI, Provider.CLAUDE).forEach { provider ->
                                FilterChip(selected = state.historyProvider == provider,
                                    onClick = { keyboard?.hide(); focusManager.clearFocus(); home.setHistoryProvider(provider) },
                                    modifier = Modifier.testTag("history-filter-${provider?.name ?: "ALL"}"),
                                    label = { Text(when (provider) { null -> stringResource(R.string.filter_all); Provider.OPENAI -> "OpenAI"; Provider.CLAUDE -> "Claude" }) })
                            }
                        }
                        if (state.historyQuery.isNotEmpty() || state.historyProvider != null) {
                            TextButton(onClick = { home.setHistoryQuery(""); home.setHistoryProvider(null); keyboard?.hide(); focusManager.clearFocus() }) {
                                Text(stringResource(R.string.history_reset))
                            }
                        }
                    }
                    if (state.historySearching && !state.loading) item("history-searching") { Text(stringResource(R.string.history_searching)) }
                    if (state.historyError != null) item("history-search-error") {
                        Column {
                            Text(state.historyError, color = MaterialTheme.colorScheme.error)
                            OutlinedButton(onClick = home::retryHistorySearch) { Text(stringResource(R.string.history_retry)) }
                        }
                    }
                    if (!state.loading && !state.historySearching && state.archiveError == null && state.historyError == null) item("history-count") {
                        Text(pluralStringResource(R.plurals.history_count, state.sessions.size, state.historyMatches.size, state.sessions.size), Modifier.testTag("history-count"))
                        if (state.sessions.isNotEmpty() && state.historyMatches.isEmpty()) Text(stringResource(R.string.history_no_matches))
                    }
                    item("new-chat") {
                        OutlinedButton(onClick = { home.newChat(); page = "conversation" }, enabled = editable) { Text(stringResource(R.string.new_chat)) }
                    }
                    if (state.sessions.isEmpty() && !state.loading && state.archiveError == null) item("empty-history") { Text(stringResource(R.string.history_empty)) }
                    items(state.historyMatches, key = { "chat-${it.conversation.id}" }) { match ->
                        val chat = match.conversation
                        OutlinedButton(onClick = { if (home.openHistoryMatch(chat.id)) { keyboard?.hide(); focusManager.clearFocus(); page = "conversation" } },
                            enabled = editable && !state.loading && state.archiveError == null && !state.historySearching,
                            modifier = Modifier.fillMaxWidth().testTag("history-${chat.id}")) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(chat.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(stringResource(R.string.chat_summary, chat.provider.displayName, chat.modelId, chat.turns.size), style = MaterialTheme.typography.bodySmall)
                                match.firstMatchingTurn?.let { Text(stringResource(R.string.message_match, it + 1), style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (exportId != null) AlertDialog(onDismissRequest = { exportId = null },
        title = { Text(stringResource(R.string.export_title)) },
        text = { Text(stringResource(R.string.export_warning)) },
        confirmButton = { TextButton(onClick = { home.prepareExport(exportId); exportId = null }, enabled = editable) { Text(stringResource(R.string.choose_location)) } },
        dismissButton = { TextButton(onClick = { exportId = null }) { Text(stringResource(R.string.cancel)) } })

    if (deleteId != null) AlertDialog(onDismissRequest = { deleteId = null },
        title = { Text(stringResource(R.string.delete_title)) }, text = { Text(stringResource(R.string.delete_warning)) },
        confirmButton = { TextButton(onClick = { home.deleteSelected(deleteId); deleteId = null }, enabled = editable) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { deleteId = null }) { Text(stringResource(R.string.cancel)) } })
}
