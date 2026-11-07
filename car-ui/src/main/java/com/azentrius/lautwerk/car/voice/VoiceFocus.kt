// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.util.Log
import com.azentrius.lautwerk.voice.Cancel
import com.azentrius.lautwerk.voice.PlaybackFocus

/** Playback and car capture acquire separate short leases; phone recognition manages its own focus. */
internal class VoiceFocus(context: Context, private val main: Handler, private val capture: Boolean = false) : PlaybackFocus {
    private val manager = context.getSystemService(AudioManager::class.java)
    override fun request(onLost: () -> Unit): Cancel? {
        var released = false
        lateinit var request: AudioFocusRequest
        fun release() {
            if (released) return
            released = true
            CarAudioResources.release.cleanup {
                check(manager.abandonAudioFocusRequest(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            }
        }
        request = AudioFocusRequest.Builder(if (capture) AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE else AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes).setAcceptsDelayedFocusGain(false).setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change ->
                if (!released && change < 0) { release(); onLost() }
            }, main).build()
        val result = runCatching { manager.requestAudioFocus(request) }.getOrNull()
        Log.i(TAG, "voice.focus capture=$capture granted=${result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED}")
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { release(); return null }
        return Cancel { release() }
    }
    companion object {
        const val TAG = "Lautwerk"
        val attributes: AudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    }
}
