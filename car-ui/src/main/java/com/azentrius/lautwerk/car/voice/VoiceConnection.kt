// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.voice

import android.content.*
import android.os.IBinder
import androidx.car.app.CarContext
import com.azentrius.lautwerk.car.R
import java.io.Closeable

/** Injectable surface contract: template tests never bind a real microphone/network service. */
interface CarVoicePort : Closeable {
    val state: CarVoiceState
    fun connect(changed: () -> Unit)
    fun start()
    fun stop()
}

class VoiceConnection(private val context: Context, private val car: CarContext? = null) : CarVoicePort {
    private var service: CarVoiceService? = null
    private var observer: Closeable? = null
    private var bound = false
    private var changed: () -> Unit = {}
    override var state = CarVoiceState()
        private set
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (!bound) return
            service = (binder as? CarVoiceService.LocalBinder)?.service
            observer = service?.observe { state = it; changed() }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            observer?.close(); observer = null; service = null
            state = CarVoiceState(status = context.getString(R.string.voice_disconnected),
                summary = context.getString(R.string.car_voice_error)); changed()
        }
    }
    override fun connect(changed: () -> Unit) {
        this.changed = changed
        if (bound) return
        bound = context.bindService(Intent(context, CarVoiceService::class.java), connection, Context.BIND_AUTO_CREATE)
    }
    override fun start() {
        val current = service
        if (current == null) {
            state = CarVoiceState(status = context.getString(R.string.voice_service_wait),
                summary = context.getString(R.string.voice_preparing)); changed()
        } else current.start(this, car, VoicePreferences(context).mode)
    }
    override fun stop() { service?.stop() }
    override fun close() {
        service?.release(this)
        observer?.close(); observer = null; service = null
        if (bound) { bound = false; context.unbindService(connection) }
        changed = {}
    }
}

class VoicePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("car_voice_v1", Context.MODE_PRIVATE)
    var mode: VoiceMode
        get() = runCatching { VoiceMode.valueOf(preferences.getString("mode", null).orEmpty()) }.getOrDefault(VoiceMode.PLAYBACK)
        set(value) { preferences.edit().putString("mode", value.name).apply() }

    // Only fixed status/error messages, never recognized text, responses or credentials.
    fun rememberOutcome(status: String, summary: String) {
        preferences.edit().putString("last_status", status).putString("last_summary", summary).apply()
    }
    fun lastOutcome() = CarVoiceState(status = preferences.getString("last_status", "").orEmpty(),
        summary = preferences.getString("last_summary", "").orEmpty())
}
