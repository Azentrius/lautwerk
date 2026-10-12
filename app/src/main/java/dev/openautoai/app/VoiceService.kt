package dev.openautoai.app

import dev.openautoai.core.i18n.Messages
import android.app.*
import android.content.res.Configuration
import dev.openautoai.core.i18n.AppLanguage
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import dev.openautoai.app.voice.*
import dev.openautoai.providers.ProviderChatClient
import dev.openautoai.settings.ModelSelectionStore
import dev.openautoai.voice.*
import java.util.concurrent.Executors

/** Explicitly started phone conversation; no CarAudioRecord or DHU integration. */
class VoiceService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newFixedThreadPool(2)
    private lateinit var controller: VoiceController
    private lateinit var interruptions: VoiceInterruptions
    private var destroying = false
    private var sessionLanguage = AppLanguage.locale().language

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, Messages.ACTIVE_CHANNEL.text(), NotificationManager.IMPORTANCE_LOW)
        )
        val credentials = SecretStore(applicationContext)
        val selection = ModelSelectionStore(applicationContext)
        val backend = ConversationVoiceBackend(ConversationStore(applicationContext), credentials,
            ProviderChatClient(credentials), AndroidVoiceTasks(worker, main), selection::forProvider,
            { selection.select(it) }, { main.post { if (!destroying) AppState.changed() } })
        val audio = PlaybackFocusAudio(AndroidVoiceAudio(this, main), AndroidPlaybackFocus(this, main))
        controller = VoiceController(audio, backend, AndroidVoiceScheduler(main), ::publish)
        interruptions = VoiceInterruptions(AndroidVoiceEnvironment(this, main)) { reason ->
            controller.stop(reason.message)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> controller.stop()
            ACTION_START -> {
                if (controller.state.active) return START_NOT_STICKY
                val id = intent.getStringExtra(EXTRA_SESSION_ID)
                if (id.isNullOrBlank()) { controller.stop(Messages.NOT_FOUND.text()); return START_NOT_STICKY }
                try {
                    // Promote before async storage/TTS initialization to satisfy the foreground deadline.
                    if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION_ID, notification(Messages.PREPARING.text()), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                    else startForeground(NOTIFICATION_ID, notification(Messages.PREPARING.text()))
                    val blocked = interruptions.start()
                    if (blocked == null) controller.start(id) else controller.stop(blocked.message)
                } catch (_: Exception) {
                    controller.stop(Messages.SERVICE_FAILED.text())
                }
            }
            else -> controller.stop()
        }
        return START_NOT_STICKY
    }

    private fun publish(state: VoiceState) {
        val conversation = state.conversation
        AppState.update(state.active, conversation?.provider ?: AppState.provider,
            conversation?.modelId ?: AppState.modelId, conversation?.id ?: AppState.sessionId, state.status, state.phase)
        AppState.reportIssue(state.notice)
        if (destroying) return
        if (state.active) {
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(state.status))
        } else {
            interruptions.close()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun notification(status: String): Notification {
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 2, Intent(this, VoiceService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("OpenAuto AI").setContentText(status).setContentIntent(open)
            .setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE)
            .addAction(Notification.Action.Builder(null, Messages.STOP_ACTION.text(), stop).build()).build()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val current = AppLanguage.locale().language
        if (sessionLanguage != current) {
            sessionLanguage = current
            controller.stop(Messages.LANGUAGE_CHANGED.text())
        }
    }

    override fun onDestroy() {
        destroying = true
        interruptions.close()
        controller.stop(controller.state.notice)
        worker.shutdownNow()
        main.removeCallbacksAndMessages(null)
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
