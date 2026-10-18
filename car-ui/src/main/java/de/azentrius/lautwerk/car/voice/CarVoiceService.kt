// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car.voice

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.*
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.car.app.CarContext
import dev.openautoai.car.R
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.StorageFailure
import dev.openautoai.providers.ProviderChatClient
import dev.openautoai.settings.ModelSelectionStore
import dev.openautoai.storage.*
import dev.openautoai.voice.*
import java.io.Closeable
import java.util.concurrent.Executors

enum class VoiceMode { PLAYBACK, MICROPHONE, CONVERSATION }
data class CarVoiceState(val active: Boolean = false, val status: String = "", val selection: ModelSelection? = null,
    val summary: String = status)

/** Bound to the visible phone/car surface. Stop, disconnect and process death never restart a session. */
class CarVoiceService : Service() {
    inner class LocalBinder : Binder() { val service get() = this@CarVoiceService }
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newFixedThreadPool(3)
    private val tasks = VoiceWorkers(worker, main)
    private val timer = VoiceTimer(main)
    private val listeners = mutableSetOf<(CarVoiceState) -> Unit>()
    var state = CarVoiceState()
        private set
    private var owner: Any? = null
    private var controller: VoiceController? = null
    private var check: AudioCheck? = null
    private var creating: Cancel? = null
    private var preparingDeadline: Cancel? = null
    private var session = 0L
    private var destroyed = false
    private val monitor = object : Runnable {
        override fun run() {
            if (!state.active) return
            val mode = getSystemService(AudioManager::class.java).mode
            if (mode != AudioManager.MODE_NORMAL) {
                fail(getString(R.string.voice_interrupted)); return
            }
            main.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        state = VoicePreferences(this).lastOutcome()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.voice_setup), NotificationManager.IMPORTANCE_LOW))
    }
    override fun onBind(intent: Intent?): IBinder = LocalBinder()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) stop()
        stopSelf(startId)
        return START_NOT_STICKY
    }
    fun observe(changed: (CarVoiceState) -> Unit): Closeable {
        listeners.add(changed); changed(state)
        return Closeable { listeners.remove(changed) }
    }
    fun start(caller: Any, car: CarContext?, mode: VoiceMode) {
        if (state.active || destroyed) return
        stop()
        if (mode != VoiceMode.PLAYBACK && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            fail(getString(R.string.voice_permission)); return
        }
        if (car != null && mode != VoiceMode.PLAYBACK && (car.carAppApiLevel < 5 || Build.VERSION.SDK_INT < 33 ||
                !SpeechRecognizer.isOnDeviceRecognitionAvailable(this))) {
            fail(getString(R.string.voice_car_unavailable)); return
        }
        if (getSystemService(AudioManager::class.java).mode != AudioManager.MODE_NORMAL) {
            fail(getString(R.string.voice_interrupted)); return
        }
        owner = caller
        val selected = ModelSelectionStore(this).load()
        try {
            val types = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK or
                (if (mode == VoiceMode.PLAYBACK) 0 else ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIFICATION, notification(getString(R.string.voice_preparing)), types)
            else startForeground(NOTIFICATION, notification(getString(R.string.voice_preparing)))
        } catch (_: Exception) {
            fail(getString(R.string.voice_foreground_failed)); return
        }
        val token = session
        publish(CarVoiceState(true, getString(R.string.voice_preparing), selected))
        main.post(monitor)
        Log.i(VoiceFocus.TAG, "voice.session.start mode=$mode car=${car != null}")
        val audio = PlaybackFocusAudio(CarVoiceAudio(this, car, main, worker, microphoneControl), VoiceFocus(this, main))
        if (mode != VoiceMode.CONVERSATION) {
            check = AudioCheck(audio, timer) { phase, problem ->
                if (token == session) {
                    Log.i(VoiceFocus.TAG, "voice.check.phase=$phase problem=${problem?.name ?: "none"}")
                    if (phase == VoicePhase.STOPPED) stop(problem?.message ?: getString(R.string.voice_check_done),
                        if (problem == null) getString(R.string.car_check_done) else errorSummary(problem.message))
                    else publish(state.copy(status = phaseText(phase), summary = phaseText(phase)))
                }
            }.also { it.start(mode == VoiceMode.MICROPHONE, getString(R.string.voice_sample)) }
        } else {
            val credentials = EncryptedCredentialStore(this)
            val conversations = EncryptedConversationRepository(this)
            val selections = ModelSelectionStore(this)
            val backend = ConversationVoiceBackend(conversations, credentials, ProviderChatClient(credentials), tasks,
                selections::forProvider, { selections.select(it) }, {})
            controller = VoiceController(audio, backend, timer) { next ->
                if (token == session) {
                    Log.i(VoiceFocus.TAG, "voice.conversation.phase=${next.phase}")
                    if (!next.active) stop(next.notice ?: next.status,
                        if (next.notice == null) getString(R.string.voice_stopped) else errorSummary(next.notice!!))
                    else publish(CarVoiceState(true, next.notice ?: next.status,
                        next.conversation?.let { ModelSelection(it.provider, it.modelId) } ?: selected, phaseText(next.phase)))
                }
            }
            preparingDeadline = timer.after(15_000) { if (token == session) fail(VoiceProblem.TIMEOUT.message) }
            creating = tasks.run({ cancel ->
                cancel.check()
                if (credentials.read(selected.provider).isNullOrBlank()) throw ChatFailure(ChatError.MISSING_KEY)
                cancel.check()
                conversations.create(selected.provider, selected.modelId)
            }) { result ->
                if (token == session) {
                    creating = null; preparingDeadline?.cancel(); preparingDeadline = null
                    result.fold({ controller?.start(it.id) }, { error -> fail(when (error) {
                        is ChatFailure -> error.message ?: getString(R.string.voice_failed)
                        is StorageFailure -> error.message ?: getString(R.string.voice_failed)
                        else -> getString(R.string.voice_failed)
                    }) })
                }
            }
        }
    }
    fun release(caller: Any) { if (owner === caller) stop() }
    private fun errorSummary(message: String) = getString(when (message) {
        VoiceProblem.NO_INPUT.message -> R.string.car_no_input
        VoiceProblem.CAPTURE_STOPPED.message -> R.string.voice_stopped
        else -> R.string.car_voice_error
    })
    private fun fail(message: String) = stop(message, errorSummary(message))
    fun stop(message: String = getString(R.string.voice_stopped), summary: String = getString(R.string.voice_stopped)) {
        val started = SystemClock.elapsedRealtime()
        Log.i(VoiceFocus.TAG, "voice.session.stop.request active=${state.active}")
        session++
        main.removeCallbacks(monitor)
        owner = null
        publish(CarVoiceState(status = message, summary = summary))
        creating?.cancel(); creating = null
        preparingDeadline?.cancel(); preparingDeadline = null
        val previous = controller; controller = null; previous?.stop()
        check?.cancel(); check = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        VoicePreferences(this).rememberOutcome(message, summary)
        Log.i(VoiceFocus.TAG, "voice.session.stop elapsedMs=${SystemClock.elapsedRealtime() - started}")
    }
    private fun phaseText(phase: VoicePhase) = getString(when (phase) {
        VoicePhase.LISTENING -> R.string.voice_listening
        VoicePhase.SPEAKING -> R.string.voice_speaking
        VoicePhase.THINKING -> R.string.car_thinking
        else -> R.string.voice_preparing
    })
    private fun publish(next: CarVoiceState) {
        state = next
        if (destroyed) return
        listeners.toList().forEach { it(next) }
        if (next.active) getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification(next.status))
    }
    private fun notification(status: String): Notification {
        val open = PendingIntent.getActivity(this, 1, Intent(this, VoiceSetupActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 2, Intent(this, CarVoiceService::class.java).setAction(STOP), PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_stat_openauto)
            .setContentTitle(getString(R.string.car_title)).setContentText(status).setContentIntent(open)
            .setVisibility(Notification.VISIBILITY_PRIVATE).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(Notification.Action.Builder(null, getString(R.string.voice_stop), stop).build()).build()
    }
    override fun onDestroy() {
        destroyed = true
        // Keep a completed/error outcome available when the user next opens phone setup.
        if (state.active) stop()
        listeners.clear(); worker.shutdownNow(); main.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
    companion object {
        // Process-owned: a newly bound service must also wait for an older host stop to finish.
        private val microphoneControl = Executors.newSingleThreadExecutor()
        private const val CHANNEL = "car_voice"
        private const val NOTIFICATION = 3418
        private const val STOP = "dev.openautoai.car.STOP"
    }
}
