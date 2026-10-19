// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import com.azentrius.lautwerk.core.i18n.Messages
import com.azentrius.lautwerk.core.ai.ChatReply
import com.azentrius.lautwerk.core.ai.Provider
import com.azentrius.lautwerk.core.storage.Conversation

fun interface Cancel { fun cancel() }
typealias CancellationCheck = com.azentrius.lautwerk.core.ai.RequestCancellation

/** Work runs off the UI thread; completion is delivered on the controller's thread. */
interface VoiceTasks {
    fun <T> run(work: (CancellationCheck) -> T, complete: (Result<T>) -> Unit): Cancel
}
fun interface VoiceScheduler { fun after(delayMillis: Long, action: () -> Unit): Cancel }

/** All calls and callbacks use one controller thread. Cancel must invalidate late callbacks. */
interface VoiceAudio {
    fun prepare(complete: (VoiceProblem?) -> Unit): Cancel
    fun listen(complete: (Recognition) -> Unit): Cancel
    fun speak(text: String, complete: (VoiceProblem?) -> Unit): Cancel
    fun close()
}

sealed interface Recognition {
    data class Text(val text: String) : Recognition
    data class Failed(val problem: VoiceProblem) : Recognition
}

enum class VoiceProblem(private val text: Messages) {
    /** Host dismissal is terminal, unlike a recognizer's retryable no-match result. */
    CAPTURE_STOPPED(Messages.ENDED),
    NO_INPUT(Messages.VOICE_NO_INPUT),
    PERMISSION(Messages.VOICE_PERMISSION),
    RECOGNIZER_UNAVAILABLE(Messages.VOICE_RECOGNIZER_UNAVAILABLE),
    RECOGNIZER_BUSY(Messages.VOICE_RECOGNIZER_BUSY),
    RECOGNITION_NETWORK(Messages.VOICE_RECOGNITION_NETWORK),
    RECOGNITION_FAILED(Messages.VOICE_RECOGNITION_FAILED),
    TTS_UNAVAILABLE(Messages.VOICE_TTS_UNAVAILABLE),
    TTS_VOICE_UNAVAILABLE(Messages.VOICE_TTS_VOICE_UNAVAILABLE),
    TTS_FAILED(Messages.VOICE_TTS_FAILED),
    AUDIO_FOCUS(Messages.VOICE_AUDIO_FOCUS),
    TIMEOUT(Messages.VOICE_TIMEOUT);
    val message: String get() = text.text()
}

interface VoiceBackend {
    fun open(id: String, complete: (Result<Conversation>) -> Unit): Cancel
    fun switchProvider(provider: Provider, complete: (Result<Conversation>) -> Unit): Cancel
    fun reply(conversation: Conversation, phrase: String, complete: (Result<ChatReply>) -> Unit): Cancel
}

enum class VoicePhase { STOPPED, PREPARING, LISTENING, THINKING, SPEAKING }
data class VoiceState(
    val phase: VoicePhase = VoicePhase.STOPPED,
    val conversation: Conversation? = null,
    val status: String = Messages.READY.text(),
    val notice: String? = null
) { val active: Boolean get() = phase != VoicePhase.STOPPED }
