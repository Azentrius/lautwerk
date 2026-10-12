package dev.openautoai.app

import android.os.Bundle
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.openautoai.app.voice.SpeechPreview
import dev.openautoai.app.voice.SpeechSettingsStore

/** Separate screen so previews stop before returning to the conversation or leaving the foreground. */
class SpeechSettingsActivity : ComponentActivity() {
    private lateinit var preview: SpeechPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preview = SpeechPreview(this, SpeechSettingsStore(this))
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) {
            SpeechSettingsContent(preview, AppState.active, ::finish)
        } } }
    }
    override fun onStart() { super.onStart(); if (!AppState.active) preview.open() }
    override fun onStop() { preview.close(); super.onStop() }
}

@Composable
private fun SpeechSettingsContent(preview: SpeechPreview, conversationActive: Boolean, back: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    var onlineChoice by remember { mutableStateOf<Voice?>(null) }
    LaunchedEffect(conversationActive) { if (conversationActive) preview.close() }
    val voices = preview.voices
    val selected = voices.find { it.name == preview.settings.voice }
    fun label(voice: Voice): String {
        val number = voices.indexOf(voice) + 1
        return "Stimme $number · ${voice.locale.getDisplayCountry(voice.locale)} · ${if (voice.isNetworkConnectionRequired) "Online" else "Auf dem Gerät"}"
    }
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Stimme und Tempo", style = MaterialTheme.typography.headlineMedium)
        Text("Für OpenAI und Claude. Änderungen gelten ab dem nächsten Gespräch.")
        if (conversationActive) Text("Bitte zuerst das laufende Gespräch beenden.")

        Text("Online-Stimmen können den vorgelesenen Text an den Android-Sprachdienst übertragen.", style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedButton(onClick = { menu = true }, enabled = preview.ready && !conversationActive, modifier = Modifier.testTag("speech-voice-picker")) {
                Text(selected?.let(::label) ?: if (preview.settings.voice == null) "Android-Standard" else "Gespeicherte Stimme nicht verfügbar")
            }
            DropdownMenu(expanded = menu && preview.ready && !conversationActive, onDismissRequest = { menu = false },
                modifier = Modifier.heightIn(max = 350.dp)) {
                DropdownMenuItem(text = { Text("Android-Standard") }, onClick = { menu = false; preview.choose(null) })
                voices.forEach { voice ->
                    DropdownMenuItem(text = { Text(label(voice)) }, onClick = {
                        menu = false
                        if (voice.isNetworkConnectionRequired) onlineChoice = voice else preview.choose(voice)
                    })
                }
            }
        }
        if (preview.ready && voices.isEmpty()) Text("Keine zusätzlichen Stimmen für deine Gerätesprache gemeldet. Android-Standard bleibt auswählbar.")
        if (!preview.ready && preview.notice == null && !conversationActive) Text("Stimmen werden geladen …")
        Text("Sprechtempo", style = MaterialTheme.typography.titleMedium)
        listOf(0.9f to "Etwas langsamer", 1f to "Normal", 1.1f to "Etwas schneller").forEach { (rate, title) ->
            FilterChip(selected = preview.settings.rate == rate, onClick = { preview.rate(rate) },
                enabled = preview.ready && !conversationActive, label = { Text(title) })
        }
        Button(onClick = preview::play, enabled = preview.ready && !preview.playing && !conversationActive,
            modifier = Modifier.fillMaxWidth()) { Text("Hörprobe abspielen") }
        OutlinedButton(onClick = preview::stop, enabled = preview.playing,
            modifier = Modifier.fillMaxWidth()) { Text("Hörprobe stoppen") }
        Text("Hörprobe mit festem Beispielsatz – ohne Mikrofon oder KI-Anfrage.", style = MaterialTheme.typography.bodySmall)
        preview.notice?.let { Text(it) }
        OutlinedButton(onClick = back, modifier = Modifier.fillMaxWidth()) { Text("Zurück zum Gespräch") }
    }
    onlineChoice?.let { voice ->
        AlertDialog(onDismissRequest = { onlineChoice = null }, title = { Text("Online-Stimme verwenden?") },
            text = { Text("Der vorgelesene Text kann an deinen Android-Sprachdienst übertragen werden – auch bei Antworten von OpenAI und Claude. Dafür wird kein zusätzlicher OpenAI- oder Claude-API-Aufruf verwendet.") },
            confirmButton = { TextButton(onClick = { if (!conversationActive) preview.choose(voice); onlineChoice = null }) { Text("Online-Stimme wählen") } },
            dismissButton = { TextButton(onClick = { onlineChoice = null }) { Text("Abbrechen") } })
    }
}
