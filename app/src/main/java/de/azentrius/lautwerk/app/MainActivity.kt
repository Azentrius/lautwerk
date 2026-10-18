// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import de.azentrius.lautwerk.core.i18n.Messages
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.azentrius.lautwerk.app.design.AzentriusTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.azentrius.lautwerk.settings.ModelSelectionStore

class MainActivity : ComponentActivity() {
    private lateinit var home: HomeViewModel
    private lateinit var startGate: ConversationStartGate
    private val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (startGate.microphoneResult(granted)) {
            if (granted) advanceStart() else failStart(Messages.MIC_PERMISSION.text())
        }
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        startGate.notificationResult() // Optional; denial still allows the foreground service.
        advanceStart()
    }

    private val exportDocument = registerForActivityResult(
        CreateChatDocument()
    ) { uri -> home.finishExportSelection(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startGate = ViewModelProvider(this)[ConversationStartGate::class.java]
        home = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
                ModelSelectionStore(applicationContext), SecretStore(applicationContext),
                ConversationStore(applicationContext), DocumentConversationExporter(applicationContext.contentResolver)) as T
        })[HomeViewModel::class.java]
        home.languageChanged()
        setContent {
            AzentriusTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(
                        home = home,
                        onStart = { conversation ->
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) requestStart(conversation)
                        },
                        onStop = ::stopConversation,
                        onSpeechSettings = { startActivity(Intent(this, SpeechSettingsActivity::class.java)) },
                        onExportDocument = { filename ->
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                try { exportDocument.launch(filename) }
                                catch (_: Exception) { home.exportPickerFailed() }
                            } else home.exportPickerFailed()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() { super.onResume(); window.decorView.post { advanceStart() } }

    override fun onStop() {
        if (!isChangingConfigurations) {
            home.cancelStart()
            home.cancelExportPreparation()
            if (startGate.sessionId != null) failStart(Messages.START_CANCELLED.text())
        }
        super.onStop()
    }

    private fun requestStart(conversation: Conversation) {
        if (AppState.active || !startGate.begin(conversation.id)) return
        AppState.reportIssue(null)
        AppState.update(true, conversation.provider, conversation.modelId, conversation.id, Messages.START_PREPARING.text())
        advanceStart()
    }

    private fun advanceStart() {
        if (!::startGate.isInitialized || startGate.sessionId == null || startGate.waiting != null ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        val microphone = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val notifications = Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        try {
            if (!microphone) {
                startGate.launched(ConversationStartGate.Permission.MICROPHONE)
                microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
            } else if (notifications && !startGate.notificationAnswered) {
                startGate.launched(ConversationStartGate.Permission.NOTIFICATIONS)
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val id = startGate.take(true, microphone, notifications) ?: return
                startForegroundService(Intent(this, VoiceService::class.java).apply {
                    action = VoiceService.ACTION_START
                    putExtra(VoiceService.EXTRA_SESSION_ID, id)
                })
            }
        } catch (_: Exception) { startGate.launchFailed(); failStart(Messages.START_FAILED.text()) }
    }

    private fun failStart(message: String) {
        startGate.cancel()
        AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, Messages.ENDED.text(), de.azentrius.lautwerk.voice.VoicePhase.STOPPED)
        AppState.reportIssue(message)
    }

    private fun stopConversation() {
        home.cancelStart()
        startGate.cancel()
        // Do not create an idle microphone service merely to stop it.
        stopService(Intent(this, VoiceService::class.java))
        AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, Messages.ENDED.text(), de.azentrius.lautwerk.voice.VoicePhase.STOPPED)
        AppState.reportIssue(null)
    }
}
