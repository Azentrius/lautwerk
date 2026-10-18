// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import de.azentrius.lautwerk.voice.Cancel
import de.azentrius.lautwerk.voice.PlaybackFocus

/** Main-thread, short-lived playback focus. Never used to initialize or run recognition. */
internal class AndroidPlaybackFocus(context: Context, private val main: Handler) : PlaybackFocus {
    private val manager = context.getSystemService(AudioManager::class.java)

    override fun request(onLost: () -> Unit): Cancel? {
        var released = false
        lateinit var request: AudioFocusRequest
        fun release() {
            if (released) return
            released = true // Ignore callbacks already queued when abandoning the request.
            runCatching { manager.abandonAudioFocusRequest(request) }
        }
        request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change ->
                if (!released && change in setOf(AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)) {
                    release()
                    onLost()
                }
            }, main).build()
        val result = runCatching { manager.requestAudioFocus(request) }.getOrNull()
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            release()
            return null
        }
        return Cancel { release() }
    }

    companion object {
        val attributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    }
}
