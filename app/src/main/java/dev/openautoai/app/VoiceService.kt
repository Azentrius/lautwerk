package dev.openautoai.app

import dev.openautoai.core.storage.ConversationRepository
import dev.openautoai.core.storage.CredentialStore
import dev.openautoai.core.storage.StorageFailure
import dev.openautoai.settings.ModelSelectionStore
import dev.openautoai.core.ai.ChatFailure
import dev.openautoai.core.ai.Completion
import dev.openautoai.core.ai.Provider
import dev.openautoai.core.ai.ModelCatalog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Foreground, user-initiated voice loop. The phone microphone is used, not CarAudioRecord.
 * Recognition/TTS are provided by the user's installed Android speech services.
 */
class VoiceService : Service(), RecognitionListener {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var active = false
    private var listening = false
    private var processing = false
    private var generation = 0
    private var provider = Provider.OPENAI
    private var modelId = ModelCatalog.default(Provider.OPENAI)
    private var sessionId: String? = null
    private val history = mutableListOf<Turn>()
    private lateinit var conversations: ConversationRepository
    private lateinit var store: CredentialStore
    private lateinit var chatApi: ChatApi

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        store = SecretStore(applicationContext)
        chatApi = ChatApi(store)
        conversations = ConversationStore(applicationContext)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Aktives KI-Gespräch", NotificationManager.IMPORTANCE_LOW)
        )
        tts = TextToSpeech(this) { status ->
            mainHandler.post {
                ttsReady = status == TextToSpeech.SUCCESS
                if (ttsReady) {
                    tts?.language = Locale.getDefault()
                    tts?.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit
                        override fun onDone(utteranceId: String?) {
                            mainHandler.post { scheduleListen(250) }
                        }
                        override fun onError(utteranceId: String?) {
                            mainHandler.post { scheduleListen(700) }
                        }
                    })
                    if (active) scheduleListen(100)
                } else if (active) {
                    update("Keine Sprachausgabe verfügbar")
                    scheduleListen(1000)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopConversation()
            ACTION_START -> {
                val id = intent.getStringExtra(EXTRA_SESSION_ID)
                try {
                    val chosen = id?.let { conversations.get(it) }
                    if (chosen == null) {
                        AppState.reportIssue("Gespräch nicht gefunden")
                        stopSelf()
                    } else if (store.read(chosen.provider).isNullOrBlank()) {
                        AppState.reportIssue("Kein API-Schlüssel für ${chosen.provider.displayName}")
                        stopSelf()
                    } else {
                        startConversation(chosen)
                    }
                } catch (error: StorageFailure) {
                    AppState.reportIssue(error.message)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startConversation(session: Conversation) {
        if (active) return
        AppState.reportIssue(null)
        provider = session.provider
        modelId = session.modelId
        sessionId = session.id
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            update("Vordergrunddienst konnte nicht gestartet werden")
            stopSelf()
            return
        }
        active = true
        history.clear()
        history.addAll(session.turns.takeLast(12))
        generation++
        update("Gespräch gestartet: ${provider.displayName}")
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            update("Keine Android-Spracherkennung installiert")
            stopConversation()
            return
        }
        try {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this).also {
                it.setRecognitionListener(this)
            }
        } catch (_: Exception) {
            update("Spracherkennung konnte nicht gestartet werden")
            stopConversation()
            return
        }
        if (ttsReady) scheduleListen(250) else update("Sprachausgabe wird vorbereitet")
    }

    private fun startListening() {
        if (!active || listening || processing) return
        val speechRecognizer = recognizer ?: return
        listening = true
        update("Ich höre zu – ${provider.displayName}")
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer.startListening(speechIntent)
        } catch (_: Exception) {
            listening = false
            update("Mikrofon derzeit nicht verfügbar")
            scheduleListen(1400)
        }
    }

    private fun scheduleListen(delayMillis: Long) {
        val expectedGeneration = generation
        mainHandler.postDelayed({
            if (active && generation == expectedGeneration && !processing && !listening) {
                startListening()
            }
        }, delayMillis)
    }

    override fun onResults(results: Bundle?) {
        listening = false
        val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.trim().orEmpty()
        if (phrase.isBlank()) {
            scheduleListen(350)
            return
        }
        val command = phrase.lowercase(Locale.ROOT)
        when {
            command == "stopp" || command == "stop" || command == "beenden" ||
                command == "gespräch beenden" -> stopConversation()
            command.startsWith("wechsle zu claude") || command.startsWith("wechsel zu claude") ->
                changeProvider(Provider.CLAUDE)
            command.startsWith("wechsle zu chatgpt") || command.startsWith("wechsel zu chatgpt") ||
                command.startsWith("wechsle zu openai") -> changeProvider(Provider.OPENAI)
            else -> sendPhrase(phrase)
        }
    }

    private fun changeProvider(newProvider: Provider) {
        val key = try { store.read(newProvider) } catch (error: StorageFailure) {
            AppState.reportIssue(error.message)
            speak(error.message.orEmpty())
            return
        }
        if (key.isNullOrBlank()) {
            speak("Für ${newProvider.displayName} ist noch kein Zugang eingerichtet.")
            return
        }
        val selections = ModelSelectionStore(applicationContext)
        val chosen = selections.forProvider(newProvider)
        val newModel = chosen.modelId
        val newSession = runCatching { conversations.create(newProvider, newModel) }.getOrElse {
            val message = (it as? StorageFailure)?.message ?: "Neues Gespräch konnte nicht erstellt werden."
            AppState.reportIssue(message)
            speak(message)
            return
        }
        selections.select(chosen)
        provider = newProvider
        modelId = newModel
        sessionId = newSession.id
        history.clear() // Deliberately do not send the former provider's transcript to a new provider.
        AppState.changed()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
        speak("Wir sprechen jetzt mit ${newProvider.displayName}. Das ist ein neues Gespräch.")
    }

    private fun sendPhrase(phrase: String) {
        if (processing || !active) return
        processing = true
        AppState.reportIssue(null)
        try {
            sessionId?.let { conversations.append(it, Turn("user", phrase)) }
            AppState.changed()
        } catch (error: Exception) {
            val message = (error as? StorageFailure)?.message ?: "Das Gespräch konnte nicht sicher gespeichert werden."
            AppState.reportIssue(message)
            processing = false
            speak(message)
            return
        }
        history += Turn("user", phrase)
        // Bound the context to a small number of exchanges to limit cost and response time.
        while (history.size > 12) history.removeAt(0)
        val messages = history.toList()
        val expectedGeneration = generation
        val requestProvider = provider
        val requestModel = modelId
        update("${provider.displayName} antwortet")
        worker.execute {
            val result = runCatching { chatApi.reply(requestProvider, requestModel, messages) }
            mainHandler.post {
                if (!active || generation != expectedGeneration) return@post
                processing = false
                result.onSuccess { response ->
                    history += Turn("assistant", response.text)
                    runCatching { sessionId?.let { conversations.append(it, Turn("assistant", response.text)) } }
                        .onFailure { AppState.reportIssue("KI-Antwort konnte nicht gespeichert werden") }
                    AppState.changed()
                    if (response.completion == Completion.TRUNCATED) {
                        val notice = "Die Antwort wurde am Limit gekürzt."
                        AppState.reportIssue(notice)
                        // The notice is local UI/speech feedback, never inserted into the archived model text.
                        speak("$notice ${response.text}")
                    } else {
                        speak(response.text)
                    }
                }.onFailure { error ->
                    val message = (error as? ChatFailure)?.message ?: "Die Anfrage ist fehlgeschlagen."
                    AppState.reportIssue(message)
                    speak(message)
                }
            }
        }
    }

    private fun speak(message: String) {
        if (!active) return
        listening = false
        update("Antwort wird gesprochen")
        if (ttsReady) {
            val result = tts?.speak(message, TextToSpeech.QUEUE_FLUSH, Bundle(), "openauto-reply")
            if (result == TextToSpeech.ERROR) scheduleListen(600)
        } else {
            update("Keine Sprachausgabe verfügbar")
            scheduleListen(900)
        }
    }

    override fun onError(error: Int) {
        listening = false
        if (!active || processing) return
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            update("Mikrofonzugriff fehlt")
            stopConversation()
        } else {
            scheduleListen(if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 1800 else 700)
        }
    }
    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun stopConversation() {
        active = false
        listening = false
        processing = false
        generation++
        history.clear()
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        update("Gespräch beendet")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun update(message: String) {
        AppState.update(active, provider, modelId, sessionId, message)
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 2, Intent(this, VoiceService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("OpenAuto AI")
            .setContentText("Sprachgespräch mit ${provider.displayName}")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(Notification.Action.Builder(null, "Beenden", stop).build())
            .build()
    }

    override fun onDestroy() {
        active = false
        generation++
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        worker.shutdownNow()
        AppState.update(false, provider, modelId, sessionId, "Gespräch beendet")
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "dev.openautoai.action.START"
        const val ACTION_STOP = "dev.openautoai.action.STOP"
        const val EXTRA_SESSION_ID = "dev.openautoai.extra.SESSION_ID"
        private const val CHANNEL = "voice_conversation"
        private const val NOTIFICATION_ID = 3417
    }
}
