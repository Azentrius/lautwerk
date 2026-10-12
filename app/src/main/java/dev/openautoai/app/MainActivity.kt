package dev.openautoai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.openautoai.settings.ModelSelectionStore

class MainActivity : ComponentActivity() {
    private lateinit var home: HomeViewModel
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

    private val exportDocument = registerForActivityResult(
        CreateChatDocument()
    ) { uri -> home.finishExportSelection(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queuedSessionId = savedInstanceState?.getString("queuedVoiceSession")
        home = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
                ModelSelectionStore(applicationContext), SecretStore(applicationContext),
                ConversationStore(applicationContext), DocumentConversationExporter(applicationContext.contentResolver)) as T
        })[HomeViewModel::class.java]
        setContent {
            MaterialTheme {
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

    override fun onStop() {
        home.cancelStart() // A backgrounded screen must not start recording after a slow disk read.
        home.cancelExportPreparation()
        super.onStop()
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
        home.cancelStart()
        queuedSessionId = null
        // Do not create an idle microphone service merely to stop it.
        stopService(Intent(this, VoiceService::class.java))
        AppState.update(false, AppState.provider, AppState.modelId, AppState.sessionId, "Gespräch beendet")
        AppState.reportIssue(null)
    }
}
