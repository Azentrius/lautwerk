package dev.openautoai.app

import dev.openautoai.core.storage.ConversationRepository
import dev.openautoai.core.storage.CredentialStore
import dev.openautoai.core.storage.StorageFailure
import dev.openautoai.core.ai.ModelSelection
import dev.openautoai.settings.ModelSelectionStore
import androidx.compose.runtime.DisposableEffect
import dev.openautoai.core.ai.Provider
import dev.openautoai.core.ai.ModelCatalog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private var queuedSessionId: String? = null
    private val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (queuedSessionId != null) {
            if (granted) requestNotificationPermission() else failStart("Mikrofon-Berechtigung fehlt. Es wurde keine Aufnahme gestartet.")
        }
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Notification permission is optional; Android still requires the foreground-service notification.
        startQueuedConversation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queuedSessionId = savedInstanceState?.getString("queuedVoiceSession")
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(
                        selections = remember { ModelSelectionStore(applicationContext) },
                        store = remember { SecretStore(applicationContext) },
                        conversations = remember { ConversationStore(applicationContext) },
                        onStart = ::requestStart,
                        onStop = ::stopConversation,
                        onSpeechSettings = { startActivity(Intent(this, SpeechSettingsActivity::class.java)) }
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("queuedVoiceSession", queuedSessionId)
        super.onSaveInstanceState(outState)
    }

    private fun requestStart(conversation: Conversation) {
        if (queuedSessionId != null || AppState.active) return
        queuedSessionId = conversation.id
        AppState.reportIssue(null)
        AppState.update(true, conversation.provider, conversation.modelId, conversation.id, "Sprachstart wird vorbereitet")
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        else requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (queuedSessionId == null) return
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else startQueuedConversation()
    }

    private fun startQueuedConversation() {
        val id = queuedSessionId ?: return
        queuedSessionId = null
        try {
            startForegroundService(Intent(this, VoiceService::class.java).apply {
                action = VoiceService.ACTION_START
                putExtra(VoiceService.EXTRA_SESSION_ID, id)
            })
        } catch (_: Exception) { failStart("Sprachdienst konnte nicht starten. Bitte die App auf dem Smartphone öffnen und erneut versuchen.") }
    }

    private fun failStart(message: String) {
        queuedSessionId = null
        AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, "Gespräch beendet")
        AppState.reportIssue(message)
    }

    private fun stopConversation() {
        queuedSessionId = null
        // Do not create an idle microphone service merely to stop it.
        stopService(Intent(this, VoiceService::class.java))
        AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, "Gespräch beendet")
        AppState.reportIssue(null)
    }
}

@Composable
internal fun MainContent(
    selections: ModelSelectionStore,
    store: CredentialStore,
    conversations: ConversationRepository,
    onStart: (Conversation) -> Unit,
    onStop: () -> Unit,
    onSpeechSettings: () -> Unit = {}
) {
    var selection by remember { mutableStateOf(selections.load()) }
    val selectedProvider = selection.provider
    val selectedModel = selection.modelId
    DisposableEffect(selections) {
        val subscription = selections.observe { selection = it }
        onDispose { subscription.close() }
    }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }
    var keyText by remember(selectedProvider) { mutableStateOf("") }
    var saveStatus by remember { mutableStateOf("") }

    // React to on-device voice service writes while activity is visible.
    val refresh = AppState.revision
    val archive = remember(conversations, refresh) { runCatching { conversations.all() } }
    val sessions = archive.getOrDefault(emptyList())
    val archiveError = archive.exceptionOrNull()?.storageMessage("Der lokale Chatverlauf konnte nicht gelesen werden.")
    val activeSession = sessions.find { it.id == selectedSessionId }
    val shownSession = if (AppState.active && AppState.sessionId != null)
        sessions.find { it.id == AppState.sessionId } ?: activeSession else activeSession

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("OpenAuto AI", style = MaterialTheme.typography.headlineMedium)
        Text("Sprach-KI auf dem Handy – Android Auto folgt experimentell")
        Text("KI-Anbieter", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Provider.entries.forEach { provider ->
                FilterChip(
                    selected = selectedProvider == provider,
                    onClick = {
                        selection = selections.selectProvider(provider)
                        selectedSessionId = null
                        saveStatus = ""
                    }, enabled = !AppState.active, label = { Text(if (provider == Provider.OPENAI) "OpenAI" else "Claude") }
                )
            }
        }
        Text("Die Auswahl bleibt gespeichert; jeder Anbieter merkt sich sein zuletzt gewähltes Modell.")
        Text("Modell – Zugang hängt von deinem API-Konto ab")
        ModelCatalog.forProvider(selectedProvider).forEach { option ->
            FilterChip(
                selected = selectedModel == option.id,
                onClick = {
                    selection = selections.select(ModelSelection(selectedProvider, option.id))
                    selectedSessionId = null
                },
                enabled = !AppState.active,
                label = { Text(option.label) }
            )
        }
        Text("Zugangsdaten (nur auf dem Smartphone)", style = MaterialTheme.typography.titleMedium)
        Text("Der Prototyp verwendet API-Schlüssel, keine Kontoanmeldung. API-Nutzung kann Kosten verursachen.")
        OutlinedTextField(
            value = keyText, onValueChange = { keyText = it },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            label = { Text("${selectedProvider.displayName}: API-Schlüssel") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                runCatching { store.save(selectedProvider, keyText) }
                    .onSuccess { keyText = ""; saveStatus = "Schlüssel verschlüsselt gespeichert" }
                    .onFailure { saveStatus = it.storageMessage("Speichern fehlgeschlagen") }
            }, enabled = keyText.isNotBlank()) { Text("Schlüssel speichern") }
            OutlinedButton(onClick = {
                runCatching { store.clear(selectedProvider) }
                    .onSuccess { keyText = ""; saveStatus = "Schlüssel entfernt" }
                    .onFailure { saveStatus = it.storageMessage("Entfernen fehlgeschlagen") }
            }) { Text("Entfernen") }
        }
        if (saveStatus.isNotBlank()) Text(saveStatus)
        OutlinedButton(onClick = onSpeechSettings, enabled = !AppState.active, modifier = Modifier.fillMaxWidth()) {
            Text("Stimme und Tempo")
        }
        Text("Gespräch", style = MaterialTheme.typography.titleMedium)
        Text("Spracheingabe und Vorlesen nutzen die Android-Sprachdienste. Diese können Audio online verarbeiten. Erkannter Text wird an den ausgewählten KI-Anbieter gesendet.")
        Text("Während der Antwort ist das Mikrofon pausiert. Beenden ist jederzeit hier oder über die Benachrichtigung möglich.")
        Text(AppState.status)
        AppState.notice?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            enabled = !AppState.active && archiveError == null &&
                (activeSession == null || ModelCatalog.valid(activeSession.provider, activeSession.modelId)),
            onClick = {
                runCatching {
                    if (store.read(selectedProvider).isNullOrBlank()) {
                        saveStatus = "Bitte zuerst einen API-Schlüssel hinterlegen"
                    } else {
                        // Continuing is only allowed for an unchanged provider/model.
                        val resume = activeSession?.takeIf {
                            it.provider == selectedProvider && it.modelId == selectedModel
                        }
                        val session = resume ?: conversations.create(selectedProvider, selectedModel)
                        selectedSessionId = session.id
                        AppState.changed()
                        onStart(session)
                    }
                }.onFailure { saveStatus = it.storageMessage("Gespräch konnte nicht geöffnet werden") }
            }, modifier = Modifier.fillMaxWidth()
        ) { Text(if (activeSession != null) "Gespräch fortsetzen" else "Sprachgespräch starten") }
        OutlinedButton(onClick = onStop, enabled = AppState.active, modifier = Modifier.fillMaxWidth()) {
            Text("Gespräch beenden")
        }
        Text("Chatverlauf (nur auf diesem Gerät)", style = MaterialTheme.typography.titleMedium)
        if (archiveError != null) {
            Text(archiveError, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = { AppState.changed() }) { Text("Verlauf erneut laden") }
        }
        OutlinedButton(onClick = { selectedSessionId = null }, enabled = !AppState.active) {
            Text("Neues Gespräch auswählen")
        }
        sessions.forEach { c ->
            OutlinedButton(
                onClick = {
                    // Do not fall back to a different model when reopening an archived chat.
                    selectedSessionId = c.id
                    if (ModelCatalog.valid(c.provider, c.modelId)) {
                        selection = selections.select(ModelSelection(c.provider, c.modelId))
                    } else {
                        saveStatus = "Das Modell dieses Chats ist nicht mehr im Katalog. Der Verlauf bleibt erhalten."
                    }
                }, modifier = Modifier.fillMaxWidth(), enabled = !AppState.active
            ) { Text("${c.title} · ${c.modelId} (${c.turns.size} Nachrichten)") }
        }
        if (shownSession != null) {
            Text("Geöffnet: ${shownSession.title}", style = MaterialTheme.typography.titleMedium)
            shownSession.turns.forEach { turn ->
                Text("${if (turn.role == "user") "Du" else "KI"}: ${turn.content}")
            }
            OutlinedButton(
                onClick = {
                    runCatching { conversations.delete(shownSession.id) }
                        .onSuccess { selectedSessionId = null; AppState.changed() }
                        .onFailure { saveStatus = it.storageMessage("Löschen fehlgeschlagen") }
                }, enabled = !AppState.active
            ) { Text("Diesen Chat löschen") }
        }
        Text("Sprachbefehle: „Wechsle zu Claude“, „Wechsle zu ChatGPT“, „Stopp“.",
            style = MaterialTheme.typography.bodySmall)
        Text("Smartphone-Sprachtest: Nach zweimaliger Stille oder zehn Minuten endet die Sitzung. Bei ausgeschaltetem Display, erkanntem Anruf oder geänderten Audiogeräten endet das Gespräch. Danach bitte erneut starten. Android-Auto-Audio ist weiterhin experimentell und nicht angebunden.",
            style = MaterialTheme.typography.bodySmall)
    }
}


private fun Throwable.storageMessage(fallback: String): String = (this as? StorageFailure)?.message ?: fallback
