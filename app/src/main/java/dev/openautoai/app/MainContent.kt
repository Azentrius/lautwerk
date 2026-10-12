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
    var page by rememberSaveable { mutableStateOf("Gespräch") }
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
    var lastNavigation by remember { mutableStateOf<Pair<String, String?>?>(null) }

    LaunchedEffect(revision) { home.refresh() }
    LaunchedEffect(page, shown?.id, state.historyFocus) {
        val navigation = page to shown?.id
        val changed = lastNavigation != navigation
        lastNavigation = navigation
        val focus = state.historyFocus
        if (page == "Gespräch" && shown != null && focus?.chatId == shown.id && focus.turnIndex in shown.turns.indices) {
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
    LaunchedEffect(state.readyStart) {
        home.takeReadyStart()?.let { keyboard?.hide(); page = "Gespräch"; onStart(it) }
    }

    LaunchedEffect(state.exportPickerPending) {
        home.takeExportFileName()?.let { keyboard?.hide(); onExportDocument(it) }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text("OpenAuto AI", style = MaterialTheme.typography.headlineSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Gespräch", "Einstellungen", "Verlauf").forEach { name ->
                            FilterChip(selected = page == name, onClick = { keyboard?.hide(); page = name },
                                label = { Text(name) }, modifier = Modifier.testTag("page-$name"))
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
                            Text("Gespräch beenden")
                        }
                    } else if (state.operation == HomeOperation.PREPARE_EXPORT) {
                        OutlinedButton(onClick = home::cancelExportPreparation, modifier = Modifier.fillMaxWidth()) {
                            Text("Export abbrechen")
                        }
                    } else if (pending) {
                        OutlinedButton(onClick = home::cancelStart, modifier = Modifier.fillMaxWidth().testTag("start-cancel")) {
                            Text("Start abbrechen")
                        }
                    } else {
                        Button(onClick = { keyboard?.hide(); home.prepareStart() },
                            enabled = editable && !state.loading && state.archiveError == null &&
                                state.selected?.let { ModelCatalog.valid(it.provider, it.modelId) } != false,
                            modifier = Modifier.fillMaxWidth().testTag("conversation-start")) {
                            Text(if (state.selectedId != null) "Gespräch fortsetzen" else "Sprachgespräch starten")
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
            if (state.loading) item("archive-loading") { Text("Verlauf wird geladen …") }
            if (state.archiveError != null) item("archive-error") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.archiveError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = home::refresh, enabled = state.operation == null) { Text("Verlauf erneut laden") }
                }
            }
            when (page) {
                "Gespräch" -> {
                    item("selected-provider") {
                        val provider = if (active) AppState.provider else shown?.provider ?: state.selection.provider
                        val model = if (active) AppState.modelId else shown?.modelId ?: state.selection.modelId
                        Text(provider.displayName, style = MaterialTheme.typography.titleLarge)
                        Text(model, style = MaterialTheme.typography.bodyMedium)
                    }
                    item("conversation-settings") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { page = "Einstellungen" }, enabled = editable, modifier = Modifier.weight(1f)) { Text("Anbieter & Modell") }
                            OutlinedButton(onClick = onSpeechSettings, enabled = editable, modifier = Modifier.weight(1f)) { Text("Stimme und Tempo") }
                        }
                    }
                    item("voice-explanation") {
                        Text("Eine Frage stellen, die Antwort anhören und weiterreden. Während der Antwort pausiert das Mikrofon.")
                        Text("Beim Zuhören: „Stopp“, „Wechsle zu Claude“ oder „Wechsle zu OpenAI“.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (shown == null) item("empty-chat") {
                        Text("Starte ein neues Gespräch oder öffne einen Chat unter Verlauf.")
                    } else {
                        item("chat-title") { Text("Geöffnet: ${shown.title}", style = MaterialTheme.typography.titleMedium) }
                        item("export-chat") {
                            OutlinedButton(onClick = { exportId = shown.id },
                                enabled = editable && !state.loading && state.archiveError == null,
                                modifier = Modifier.testTag("export-chat")) { Text("Als Textdatei exportieren") }
                        }
                        if (shown.turns.size > visibleTurns) item("older-turns") {
                            OutlinedButton(onClick = { visibleTurns += 50 }) { Text("Ältere Nachrichten anzeigen") }
                        }
                        val first = (shown.turns.size - visibleTurns).coerceAtLeast(0)
                        items(shown.turns.size - first, key = { "turn-${shown.id}-${first + it}" }) { offset ->
                            val turn = shown.turns[first + offset]
                            Text("${if (turn.role == "user") "Du" else "KI"}: ${turn.content}",
                                Modifier.testTag("chat-turn-${first + offset}"))
                        }
                        item("delete-chat") {
                            OutlinedButton(onClick = { deleteId = shown.id }, enabled = editable && !state.loading && state.archiveError == null) {
                                Text("Diesen Chat löschen")
                            }
                        }
                    }
                    item("voice-limits") {
                        Text("Display aus oder eine erkannte Unterbrechung beendet das Gespräch. Danach bitte erneut starten. Zweimalige Stille oder zehn Minuten beenden die Sitzung ebenfalls.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                "Einstellungen" -> {
                    item("providers") {
                        Text("KI-Anbieter", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Provider.entries.forEach { provider ->
                                FilterChip(selected = state.selection.provider == provider,
                                    onClick = { home.selectProvider(provider) }, enabled = editable,
                                    label = { Text(if (provider == Provider.OPENAI) "OpenAI" else "Claude") })
                            }
                        }
                    }
                    item("models-label") { Text("Modell – Zugang hängt von deinem API-Konto ab") }
                    items(ModelCatalog.forProvider(state.selection.provider), key = { "model-${it.id}" }) { model ->
                        FilterChip(selected = state.selection.modelId == model.id,
                            onClick = { home.selectModel(ModelSelection(state.selection.provider, model.id)) },
                            enabled = editable, label = { Text(model.label) })
                    }
                    item("key-label") {
                        Text("Zugangsdaten", style = MaterialTheme.typography.titleMedium)
                        Text("Eigener API-Schlüssel für ${state.selection.provider.displayName}. API-Nutzung kann Kosten verursachen.")
                    }
                    item("key-input") {
                        OutlinedTextField(value = keyText, onValueChange = { keyText = it }, enabled = editable,
                            visualTransformation = PasswordVisualTransformation(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("api-key-input"),
                            label = { Text("${state.selection.provider.displayName}: API-Schlüssel") })
                    }
                    item("key-actions") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if (home.saveKey(keyText)) { keyText = ""; keyboard?.hide() } },
                                enabled = editable && keyText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Schlüssel speichern") }
                            OutlinedButton(onClick = { if (home.clearKey()) keyText = "" }, enabled = editable) { Text("Entfernen") }
                        }
                    }
                    item("privacy") {
                        Text("Gespeicherte Schlüssel werden hier nicht angezeigt. Chats und Schlüssel werden auf diesem Gerät verschlüsselt gespeichert. Ein von dir gewählter Textexport ist unverschlüsselt. Android-Sprachdienste können Audio online verarbeiten; der gewählte Anbieter erhält den erkannten Text.")
                        Text("Android-Auto-Audio ist noch nicht angebunden. Die DHU-Test-App bleibt separat.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                "Verlauf" -> {
                    item("history-search") {
                        OutlinedTextField(value = state.historyQuery, onValueChange = home::setHistoryQuery,
                            singleLine = true, modifier = Modifier.fillMaxWidth().testTag("history-search"),
                            label = { Text("Chats durchsuchen") },
                            supportingText = { Text("Titel, Anbieter, Modell und alle Nachrichten · bis 200 Zeichen") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focusManager.clearFocus() }))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf<Provider?>(null, Provider.OPENAI, Provider.CLAUDE).forEach { provider ->
                                FilterChip(selected = state.historyProvider == provider,
                                    onClick = { keyboard?.hide(); focusManager.clearFocus(); home.setHistoryProvider(provider) },
                                    modifier = Modifier.testTag("history-filter-${provider?.name ?: "ALL"}"),
                                    label = { Text(when (provider) { null -> "Alle"; Provider.OPENAI -> "OpenAI"; Provider.CLAUDE -> "Claude" }) })
                            }
                        }
                        if (state.historyQuery.isNotEmpty() || state.historyProvider != null) {
                            TextButton(onClick = { home.setHistoryQuery(""); home.setHistoryProvider(null); keyboard?.hide(); focusManager.clearFocus() }) {
                                Text("Suche zurücksetzen")
                            }
                        }
                    }
                    if (state.historySearching && !state.loading) item("history-searching") { Text("Verlauf wird durchsucht …") }
                    if (state.historyError != null) item("history-search-error") {
                        Column {
                            Text(state.historyError, color = MaterialTheme.colorScheme.error)
                            OutlinedButton(onClick = home::retryHistorySearch) { Text("Suche erneut versuchen") }
                        }
                    }
                    if (!state.loading && !state.historySearching && state.archiveError == null && state.historyError == null) item("history-count") {
                        Text("${state.historyMatches.size} von ${state.sessions.size} Gesprächen", Modifier.testTag("history-count"))
                        if (state.sessions.isNotEmpty() && state.historyMatches.isEmpty()) Text("Keine passenden Gespräche. Suche ändern oder zurücksetzen.")
                    }
                    item("new-chat") {
                        OutlinedButton(onClick = { home.newChat(); page = "Gespräch" }, enabled = editable) { Text("Neues Gespräch auswählen") }
                    }
                    if (state.sessions.isEmpty() && !state.loading && state.archiveError == null) item("empty-history") { Text("Noch keine gespeicherten Gespräche.") }
                    items(state.historyMatches, key = { "chat-${it.conversation.id}" }) { match ->
                        val chat = match.conversation
                        OutlinedButton(onClick = { if (home.openHistoryMatch(chat.id)) { keyboard?.hide(); focusManager.clearFocus(); page = "Gespräch" } },
                            enabled = editable && !state.loading && state.archiveError == null && !state.historySearching,
                            modifier = Modifier.fillMaxWidth().testTag("history-${chat.id}")) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(chat.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${chat.provider.displayName} · ${chat.modelId} · ${chat.turns.size} Nachrichten", style = MaterialTheme.typography.bodySmall)
                                match.firstMatchingTurn?.let { Text("Treffer in Nachricht ${it + 1}", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (exportId != null) AlertDialog(onDismissRequest = { exportId = null },
        title = { Text("Chat als Textdatei exportieren?") },
        text = { Text("Die Datei enthält den Titel, Anbieter, Modell und alle Nachrichten dieses Chats als unverschlüsselten Text. Gespeicherte API-Zugangsdaten werden nicht übernommen. Persönliche Angaben in deinen Nachrichten bleiben enthalten. Du wählst gleich den Speicherort; bei einem Cloud-Dienst wird die Datei dorthin übertragen.") },
        confirmButton = { TextButton(onClick = { home.prepareExport(exportId); exportId = null }, enabled = editable) { Text("Speicherort wählen") } },
        dismissButton = { TextButton(onClick = { exportId = null }) { Text("Abbrechen") } })

    if (deleteId != null) AlertDialog(onDismissRequest = { deleteId = null },
        title = { Text("Gespräch löschen?") }, text = { Text("Dieses lokale Gespräch wird dauerhaft gelöscht.") },
        confirmButton = { TextButton(onClick = { home.deleteSelected(deleteId); deleteId = null }, enabled = editable) { Text("Löschen") } },
        dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Abbrechen") } })
}
