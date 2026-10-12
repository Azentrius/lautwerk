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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import dev.openautoai.core.i18n.AppLanguage
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
    val context = LocalContext.current
    fun label(voice: Voice): String {
        val number = voices.indexOf(voice) + 1
        return context.getString(R.string.voice_label, number, voice.locale.getDisplayCountry(AppLanguage.locale()), context.getString(if (voice.isNetworkConnectionRequired) R.string.online else R.string.on_device))
    }
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.voice_settings), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.voice_hint))
        if (conversationActive) Text(stringResource(R.string.stop_first))

        Text(stringResource(R.string.online_voice_hint), style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedButton(onClick = { menu = true }, enabled = preview.ready && !conversationActive, modifier = Modifier.testTag("speech-voice-picker")) {
                Text(selected?.let(::label) ?: if (preview.settings.voice == null) stringResource(R.string.android_default) else stringResource(R.string.voice_missing))
            }
            DropdownMenu(expanded = menu && preview.ready && !conversationActive, onDismissRequest = { menu = false },
                modifier = Modifier.heightIn(max = 350.dp)) {
                DropdownMenuItem(text = { Text(stringResource(R.string.android_default)) }, onClick = { menu = false; preview.choose(null) })
                voices.forEach { voice ->
                    DropdownMenuItem(text = { Text(label(voice)) }, onClick = {
                        menu = false
                        if (voice.isNetworkConnectionRequired) onlineChoice = voice else preview.choose(voice)
                    })
                }
            }
        }
        if (preview.ready && voices.isEmpty()) Text(stringResource(R.string.voices_empty))
        if (!preview.ready && preview.notice == null && !conversationActive) Text(stringResource(R.string.voices_loading))
        Text(stringResource(R.string.speech_rate), style = MaterialTheme.typography.titleMedium)
        listOf(0.9f to stringResource(R.string.rate_slow), 1f to stringResource(R.string.rate_normal), 1.1f to stringResource(R.string.rate_fast)).forEach { (rate, title) ->
            FilterChip(selected = preview.settings.rate == rate, onClick = { preview.rate(rate) },
                enabled = preview.ready && !conversationActive, label = { Text(title) })
        }
        Button(onClick = preview::play, enabled = preview.ready && !preview.playing && !conversationActive,
            modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.preview_play)) }
        OutlinedButton(onClick = preview::stop, enabled = preview.playing,
            modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.preview_stop)) }
        Text(stringResource(R.string.preview_hint), style = MaterialTheme.typography.bodySmall)
        preview.notice?.let { Text(it) }
        OutlinedButton(onClick = back, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back_conversation)) }
    }
    onlineChoice?.let { voice ->
        AlertDialog(onDismissRequest = { onlineChoice = null }, title = { Text(stringResource(R.string.online_voice_title)) },
            text = { Text(stringResource(R.string.online_voice_warning)) },
            confirmButton = { TextButton(onClick = { if (!conversationActive) preview.choose(voice); onlineChoice = null }) { Text(stringResource(R.string.online_voice_choose)) } },
            dismissButton = { TextButton(onClick = { onlineChoice = null }) { Text(stringResource(R.string.cancel)) } })
    }
}
