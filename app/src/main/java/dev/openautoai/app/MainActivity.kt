package dev.openautoai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
    private val microphoneRequestCode = 101
    private var queuedSession: Conversation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(
                        store = remember { SecretStore(applicationContext) },
                        conversations = remember { ConversationStore(applicationContext) },
                        onStart = ::requestStart,
                        onStop = ::stopConversation
                    )
                }
            }
        }
    }

    private fun requestStart(conversation: Conversation) {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            queuedSession = conversation
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), microphoneRequestCode)
            return
        }
        val intent = Intent(this, VoiceService::class.java).apply {
            action = VoiceService.ACTION_START
            putExtra(VoiceService.EXTRA_SESSION_ID, conversation.id)
        }
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }

    private fun stopConversation() {
        startService(Intent(this, VoiceService::class.java).apply { action = VoiceService.ACTION_STOP })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == microphoneRequestCode) {
            val session = queuedSession
            queuedSession = null
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED && session != null) {
                requestStart(session)
            } else {
                AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId,
                    "Mikrofon-Berechtigung fehlt")
            }
        }
    }
}

@Composable
private fun MainContent(
    store: SecretStore,
    conversations: ConversationStore,
    onStart: (Conversation) -> Unit,
    onStop: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(Provider.OPENAI) }
    var selectedModel by remember { mutableStateOf(ModelCatalog.default(Provider.OPENAI)) }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }
    var keyText by remember(selectedProvider) { mutableStateOf("") }
    var saveStatus by remember { mutableStateOf("") }

    // React to on-device voice service writes while activity is visible.
    val refresh = AppState.revision
    val sessions = remember(refresh) { conversations.all() }
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
                        selectedProvider = provider
                        selectedModel = ModelCatalog.default(provider)
                        selectedSessionId = null
                        saveStatus = ""
                    }, label = { Text(if (provider == Provider.OPENAI) "ChatGPT" else "Claude") }
                )
            }
        }
        Text("Modell – Zugang hängt von deinem API-Konto ab")
        ModelCatalog.forProvider(selectedProvider).forEach { option ->
            FilterChip(
                selected = selectedModel == option.id,
                onClick = { selectedModel = option.id; selectedSessionId = null },
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
                    .onFailure { saveStatus = "Speichern fehlgeschlagen" }
            }, enabled = keyText.isNotBlank()) { Text("Schlüssel speichern") }
            OutlinedButton(onClick = {
                store.clear(selectedProvider)
                keyText = ""
                saveStatus = "Schlüssel entfernt"
            }) { Text("Entfernen") }
        }
        if (saveStatus.isNotBlank()) Text(saveStatus)
        Text("Gespräch", style = MaterialTheme.typography.titleMedium)
        Text(AppState.status)
        Button(
            enabled = !AppState.active,
            onClick = {
                if (store.read(selectedProvider).isNullOrBlank()) {
                    saveStatus = "Bitte zuerst einen API-Schlüssel hinterlegen"
                } else {
                    runCatching {
                        // Continuing is only allowed for an unchanged provider/model.
                        val resume = activeSession?.takeIf {
                            it.provider == selectedProvider && it.modelId == selectedModel
                        }
                        val session = resume ?: conversations.create(selectedProvider, selectedModel)
                        selectedSessionId = session.id
                        AppState.changed()
                        onStart(session)
                    }.onFailure { saveStatus = it.message ?: "Gespräch konnte nicht geöffnet werden" }
                }
            }, modifier = Modifier.fillMaxWidth()
        ) { Text(if (activeSession != null) "Gespräch fortsetzen" else "Sprachgespräch starten") }
        OutlinedButton(onClick = onStop, enabled = AppState.active, modifier = Modifier.fillMaxWidth()) {
            Text("Gespräch beenden")
        }
        Text("Chatverlauf (nur auf diesem Gerät)", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { selectedSessionId = null }, enabled = !AppState.active) {
            Text("Neues Gespräch auswählen")
        }
        sessions.forEach { c ->
            OutlinedButton(
                onClick = {
                    selectedSessionId = c.id
                    selectedProvider = c.provider
                    selectedModel = c.modelId
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
                        .onFailure { saveStatus = it.message ?: "Löschen fehlgeschlagen" }
                }, enabled = !AppState.active
            ) { Text("Diesen Chat löschen") }
        }
        Text("Sprachbefehle: „Wechsle zu Claude“, „Wechsle zu ChatGPT“, „Stopp“.",
            style = MaterialTheme.typography.bodySmall)
        Text("Experimentell: Noch keine Android-Auto-Display-Integration. Auto-Audio und dauerhafte Erkennung bei gesperrtem Gerät müssen praktisch getestet werden.",
            style = MaterialTheme.typography.bodySmall)
    }
}
