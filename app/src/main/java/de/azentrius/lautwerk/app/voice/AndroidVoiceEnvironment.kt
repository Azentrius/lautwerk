// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app.voice

import android.app.KeyguardManager
import android.content.*
import android.media.*
import android.os.*
import de.azentrius.lautwerk.voice.*

/** Read-only system signals. No phone-state permission, call details or forced audio routing. */
internal class AndroidVoiceEnvironment(private val context: Context, private val main: Handler) : VoiceEnvironment {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val power = context.getSystemService(PowerManager::class.java)
    private val keyguard = context.getSystemService(KeyguardManager::class.java)

    override fun current() = AudioEnvironment(
        screenAvailable = power.isInteractive && !keyguard.isKeyguardLocked,
        communicationBusy = communicationBusy(audio.mode),
        // Input endpoints may appear briefly when SpeechRecognizer opens a microphone.
        // Watch connected external OUTPUT devices, not the recognizer's transient input routing.
        externalOutputs = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.type in EXTERNAL_TYPES }.map { it.id }.toSet()
    )

    override fun observe(changed: (AudioEnvironment) -> Unit): Cancel {
        var active = true
        var receiverRegistered = false
        var devicesRegistered = false
        var removeModeListener: (() -> Unit)? = null
        fun inspect() { if (active) changed(current()) }
        val poll = object : Runnable {
            override fun run() { inspect(); if (active) main.postDelayed(this, 500) }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (!active) return
                if (intent?.action == Intent.ACTION_SCREEN_OFF)
                    changed(current().copy(screenAvailable = false))
                else if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    // The device callback/poll compares the actual connected outputs.
                    inspect()
                }
            }
        }
        val devices = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) = inspect()
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) = inspect()
        }
        val cleanup = Cancel {
            active = false
            main.removeCallbacks(poll)
            if (receiverRegistered) { receiverRegistered = false; runCatching { context.unregisterReceiver(receiver) } }
            if (devicesRegistered) { devicesRegistered = false; runCatching { audio.unregisterAudioDeviceCallback(devices) } }
            removeModeListener?.let { removeModeListener = null; runCatching(it) }
        }
        try {
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply { addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY) }
            if (Build.VERSION.SDK_INT >= 33) context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            else context.registerReceiver(receiver, filter)
            receiverRegistered = true
            audio.registerAudioDeviceCallback(devices, main)
            devicesRegistered = true
            if (Build.VERSION.SDK_INT >= 31) {
                val listener = AudioManager.OnModeChangedListener { mode ->
                    // Preserve even short interruption events that ended before dispatch.
                    if (active) changed(current().let { it.copy(communicationBusy = it.communicationBusy || communicationBusy(mode)) })
                }
                audio.addOnModeChangedListener(context.mainExecutor, listener)
                removeModeListener = { audio.removeOnModeChangedListener(listener) }
            }
            // Covers lock state and Android 10/11, which have no audio-mode listener.
            main.postDelayed(poll, 500)
        } catch (error: Exception) { cleanup.cancel(); throw error }
        return cleanup
    }

    companion object {
        private fun communicationBusy(mode: Int) = mode == AudioManager.MODE_RINGTONE ||
            mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION ||
            (Build.VERSION.SDK_INT >= 30 && mode == AudioManager.MODE_CALL_SCREENING) ||
            (Build.VERSION.SDK_INT >= 33 &&
                (mode == AudioManager.MODE_CALL_REDIRECT || mode == AudioManager.MODE_COMMUNICATION_REDIRECT))

        private val EXTERNAL_TYPES = buildSet {
            addAll(setOf(AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_ACCESSORY,
                AudioDeviceInfo.TYPE_HEARING_AID))
            if (Build.VERSION.SDK_INT >= 31) addAll(setOf(AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER))
            if (Build.VERSION.SDK_INT >= 33) add(AudioDeviceInfo.TYPE_BLE_BROADCAST)
        }
    }
}
