// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.voice

/** Returns a release handle only when playback is authorized. All callbacks use the caller's thread. */
fun interface PlaybackFocus { fun request(onLost: () -> Unit): Cancel? }

/** Recognition manages its own focus. Hold our focus only while reading an answer aloud. */
class PlaybackFocusAudio(private val audio: VoiceAudio, private val focus: PlaybackFocus) : VoiceAudio {
    private var playback: Playback? = null

    override fun prepare(complete: (VoiceProblem?) -> Unit): Cancel {
        playback?.cancel()
        return audio.prepare(complete)
    }

    override fun listen(complete: (Recognition) -> Unit): Cancel {
        playback?.cancel()
        return audio.listen(complete)
    }

    override fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel {
        playback?.cancel()
        val current = Playback(complete)
        playback = current
        current.start(text)
        return Cancel { current.cancel() }
    }

    override fun close() {
        playback?.cancel()
        audio.close()
    }

    private inner class Playback(private val complete: (VoiceProblem?) -> Unit) {
        private var active = true
        private var lease: Cancel? = null
        private var speech: Cancel? = null

        fun start(text: String) {
            val acquired = runCatching { focus.request { finish(VoiceProblem.AUDIO_FOCUS) } }.getOrNull()
            if (!active) { acquired?.cancel(); return }
            if (acquired == null) { finish(VoiceProblem.AUDIO_FOCUS); return }
            lease = acquired
            try {
                val speaking = audio.speak(text, ::finish)
                if (active) speech = speaking else speaking.cancel()
            } catch (_: Exception) { finish(VoiceProblem.TTS_FAILED) }
        }

        private fun finish(problem: VoiceProblem?) {
            if (!active) return
            cancel() // Stop/release before a callback can start another recognition or playback.
            complete(problem)
        }

        fun cancel() {
            if (!active) return
            active = false
            if (playback === this) playback = null
            runCatching { speech?.cancel() }
            speech = null
            runCatching { lease?.cancel() }
            lease = null
        }
    }
}
