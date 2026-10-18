// SPDX-License-Identifier: Apache-2.0
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.azentrius.lautwerk.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.*
import com.azentrius.lautwerk.app.design.*
import com.azentrius.lautwerk.app.design.AzPrimaryButton as Button
import com.azentrius.lautwerk.app.design.AzSecondaryButton as OutlinedButton
import com.azentrius.lautwerk.app.design.AzTextButton as TextButton
import com.azentrius.lautwerk.app.design.AzChip as FilterChip
import com.azentrius.lautwerk.app.design.AzTextField as OutlinedTextField
import com.azentrius.lautwerk.app.design.AzDialog as AlertDialog
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azentrius.lautwerk.core.ai.*

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
    var licenses by rememberSaveable { mutableStateOf(false) }
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
            // Provider, settings, explanation, title; optionally the older-messages button.
            val index = notices + 4 + (if (first > 0) 1 else 0) + focus.turnIndex - first
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
        containerColor = Az.colors["bg-page"],
        topBar = {
            Column(Modifier.fillMaxWidth().background(Az.colors["bg-page"]).statusBarsPadding()) {
                val name = stringResource(R.string.app_name)
                val signature = Az.colors["signature"]
                // Word mark: the name in app-title, directly followed by the dot in signature.
                // Screen readers read only the name, not the dot as punctuation.
                val title = buildAnnotatedString {
                    append(name)
                    withStyle(SpanStyle(color = signature)) { append('.') }
                }
                Text(title, style = AzType.title, color = Az.colors["text"],
                    modifier = Modifier.padding(horizontal = Az.inset, vertical = 8.dp)
                        .semantics { contentDescription = name }
                        .testTag("app-title"))
                AzTabs(page) { keyboard?.hide();page=it }
            }
        },
        bottomBar = {
            ConversationBar(state,{ keyboard?.hide();home.prepareStart() },onStop,home::cancelStart,
                home::cancelExportPreparation,{ shown?.let { exportId=it.id } })
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).testTag("home-list"),
            state = list, contentPadding = PaddingValues(Az.inset), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (state.message != null) item("storage-message") { Text(state.message, Modifier.testTag("storage-message")) }
            if (AppState.notice != null) item("voice-notice") { Text(AppState.notice!!, color = Az.colors["danger-text"]) }
            if (state.loading) item("archive-loading") { Text(stringResource(R.string.history_loading)) }
            if (state.archiveError != null) item("archive-error") {
                Spacer(Modifier.height(Az.inset))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.archiveError, color = Az.colors["danger-text"])
                    OutlinedButton(onClick = home::refresh, enabled = state.operation == null) { Text(stringResource(R.string.history_reload)) }
                }
            }
            if(state.operation in listOf(HomeOperation.SAVE_KEY,HomeOperation.CLEAR_KEY,HomeOperation.DELETE_CHAT)) item("operation-message") {
                Text(state.operation!!.label)
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
                        Spacer(Modifier.height(Az.inset))
                        AzActions {
                            OutlinedButton(onClick = { page = "settings" }, enabled = editable) { Text(stringResource(R.string.provider_model)) }
                            OutlinedButton(onClick = onSpeechSettings, enabled = editable) { Text(stringResource(R.string.voice_settings)) }
                        }
                    }
                    item("voice-explanation") {
                        Spacer(Modifier.height(Az.inset))
                        Text(stringResource(R.string.conversation_hint))
                        Text(stringResource(R.string.command_hint), color=Az.colors["text-subtle"], style = MaterialTheme.typography.bodySmall)
                    }
                    if (shown == null) item("empty-chat") {
                        Spacer(Modifier.height(Az.inset))
                        Text(stringResource(R.string.empty_chat))
                    } else {
                        item("chat-title") { Text(stringResource(R.string.opened_chat, shown.title), style = MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=24.dp,bottom=8.dp)) }
                        if (shown.turns.size > visibleTurns) item("older-turns") {
                            Spacer(Modifier.height(Az.inset))
                            OutlinedButton(onClick = { visibleTurns += 50 }) { Text(stringResource(R.string.older_messages)) }
                        }
                        val first = (shown.turns.size - visibleTurns).coerceAtLeast(0)
                        items(shown.turns.size - first, key = { "turn-${shown.id}-${first + it}" }) { offset ->
                            val turn = shown.turns[first + offset]
                            val index=first+offset
                            val gap=if(index>first && shown.turns[index-1].role==turn.role)8.dp else 24.dp
                            AzMessage(turn.role,turn.content,stringResource(R.string.message_content,
                                stringResource(if(turn.role=="user")R.string.user_role else R.string.az_assistant_role),turn.content),
                                Modifier.padding(top=gap).testTag("chat-turn-$index"))
                        }
                        item("delete-chat") {
                            Spacer(Modifier.height(Az.inset))
                            OutlinedButton(onClick = { deleteId = shown.id }, enabled = editable && !state.loading && state.archiveError == null) {
                                Text(stringResource(R.string.delete_chat))
                            }
                        }
                    }
                    item("voice-limits") {
                        Spacer(Modifier.height(Az.inset))
                        Text(stringResource(R.string.conversation_limits), color=Az.colors["text-subtle"], style = MaterialTheme.typography.bodySmall)
                    }
                }
                "settings" -> {
                    item("providers") {
                        Spacer(Modifier.height(Az.inset))
                        Text(stringResource(R.string.provider_label), style = MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=8.dp,bottom=8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Provider.entries.forEach { provider ->
                                FilterChip(selected = state.selection.provider == provider,
                                    onClick = { home.selectProvider(provider) }, enabled = editable,
                                    label = { Text(if (provider == Provider.OPENAI) "OpenAI" else "Claude") })
                            }
                        }
                    }
                    item("models-label") { Text(stringResource(R.string.model_access)) }
                    item("models") {
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            ModelCatalog.forProvider(state.selection.provider).forEach { model ->
                                FilterChip(selected=state.selection.modelId==model.id,
                                    onClick={ home.selectModel(ModelSelection(state.selection.provider,model.id)) },
                                    enabled=editable,label={Text(model.label)})
                            }
                        }
                    }
                    item("key-label") {
                        Spacer(Modifier.height(Az.inset))
                        Text(stringResource(R.string.credentials), style = MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=8.dp,bottom=8.dp))
                        Text(stringResource(R.string.key_hint, state.selection.provider.displayName))
                    }
                    item("key-input") {
                        Spacer(Modifier.height(Az.inset))
                        OutlinedTextField(value = keyText, onValueChange = { keyText = it }, enabled = editable,
                            visualTransformation = PasswordVisualTransformation(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("api-key-input"),
                            fieldLabel=stringResource(R.string.key_label,state.selection.provider.displayName),
                            label = { Text(stringResource(R.string.key_label, state.selection.provider.displayName)) })
                    }
                    item("key-actions") {
                        Spacer(Modifier.height(Az.inset))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if (home.saveKey(keyText)) { keyText = ""; keyboard?.hide() } },
                                enabled = editable && keyText.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.key_save)) }
                            OutlinedButton(onClick = { if (home.clearKey()) keyText = "" }, enabled = editable) { Text(stringResource(R.string.remove)) }
                        }
                    }
                    item("licenses") { TextButton(onClick={licenses=true}) { Text(stringResource(R.string.az_licenses)) } }
                    item("privacy") {
                        Spacer(Modifier.height(Az.inset))
                        Text(stringResource(R.string.privacy))
                        Text(stringResource(R.string.auto_audio_pending), color=Az.colors["text-subtle"], style = MaterialTheme.typography.bodySmall)
                    }
                }
                "history" -> {
                    item("history-search") {
                        Spacer(Modifier.height(Az.inset))
                        OutlinedTextField(value = state.historyQuery, onValueChange = home::setHistoryQuery,
                            singleLine = true, modifier = Modifier.fillMaxWidth().testTag("history-search"),
                            fieldLabel=stringResource(R.string.history_search),
                            label = { Text(stringResource(R.string.history_search)) },
                            supportingText = { Text(stringResource(R.string.history_search_hint)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); focusManager.clearFocus() }))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Spacer(Modifier.height(Az.inset))
                        Column {
                            Text(state.historyError, color = Az.colors["danger-text"])
                            OutlinedButton(onClick = home::retryHistorySearch) { Text(stringResource(R.string.history_retry)) }
                        }
                    }
                    if (!state.loading && !state.historySearching && state.archiveError == null && state.historyError == null) item("history-count") {
                        Spacer(Modifier.height(Az.inset))
                        Text(pluralStringResource(R.plurals.history_count, state.sessions.size, state.historyMatches.size, state.sessions.size), Modifier.testTag("history-count"))
                        if (state.sessions.isNotEmpty() && state.historyMatches.isEmpty()) Text(stringResource(R.string.history_no_matches))
                    }
                    item("new-chat") {
                        Spacer(Modifier.height(Az.inset))
                        OutlinedButton(onClick = { home.newChat(); page = "conversation" }, enabled = editable) { Text(stringResource(R.string.new_chat)) }
                        Spacer(Modifier.height(16.dp))
                    }
                    if (state.sessions.isEmpty() && !state.loading && state.archiveError == null) item("empty-history") {
                        Column(Modifier.padding(vertical=48.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                            CompositionLocalProvider(LocalContentColor provides Az.colors["text-subtle"]) { AzIcon(R.drawable.ic_az_history,48.dp) }
                            Text(stringResource(R.string.az_empty_history),style=AzType.dialog)
                            Text(stringResource(R.string.history_empty),color=Az.colors["text-subtle"])
                        }
                    }
                    items(state.historyMatches.size, key = { "chat-${state.historyMatches[it].conversation.id}" }) { index ->
                        val match=state.historyMatches[index]
                        val chat = match.conversation
                        AzHistoryRow(chat.title,"${historyDate(chat.updatedAt)} · ${chat.provider.displayName} · ${chat.modelId}",
                            match.firstMatchingTurn?.let { stringResource(R.string.message_match,it+1) },
                            { if(home.openHistoryMatch(chat.id)) { keyboard?.hide();focusManager.clearFocus();page="conversation" } },
                            enabled=editable&&!state.loading&&state.archiveError==null&&!state.historySearching,
                            first=index==0,last=index==state.historyMatches.lastIndex,
                            modifier=Modifier.testTag("history-${chat.id}"))
                    }
                }
            }
        }
    }

    if(licenses) LicenseDialog { licenses=false }

    if (exportId != null) AlertDialog(onDismissRequest = { exportId = null },
        title = { Text(stringResource(R.string.export_title)) },
        text = { Text(stringResource(R.string.export_warning)) },
        confirmButton = { Button(onClick = { home.prepareExport(exportId); exportId = null }, enabled = editable) { Text(stringResource(R.string.choose_location)) } },
        dismissButton = { TextButton(onClick = { exportId = null }) { Text(stringResource(R.string.cancel)) } })

    if (deleteId != null) AlertDialog(onDismissRequest = { deleteId = null },
        title = { Text(stringResource(R.string.delete_title)) }, text = { Text(stringResource(R.string.delete_warning)) },
        confirmButton = { AzButton(kind=AzButtonKind.DANGER,onClick = { home.deleteSelected(deleteId); deleteId = null }, enabled = editable) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { deleteId = null }) { Text(stringResource(R.string.cancel)) } })
}
