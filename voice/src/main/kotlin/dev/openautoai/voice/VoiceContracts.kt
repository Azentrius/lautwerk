package dev.openautoai.voice

import dev.openautoai.core.ai.ChatReply
import dev.openautoai.core.ai.Provider
import dev.openautoai.core.storage.Conversation

fun interface Cancel { fun cancel() }
fun interface CancellationCheck { fun check() }

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

enum class VoiceProblem(val message: String) {
    NO_INPUT("Keine Sprache erkannt. Das Gespräch wurde beendet."),
    PERMISSION("Mikrofonzugriff fehlt. Bitte die Berechtigung auf dem Smartphone prüfen."),
    RECOGNIZER_UNAVAILABLE("Keine nutzbare Android-Spracherkennung verfügbar."),
    RECOGNIZER_BUSY("Das Mikrofon oder die Spracherkennung ist gerade belegt."),
    RECOGNITION_NETWORK("Die Spracherkennung hat keine Verbindung. Bitte später erneut starten."),
    RECOGNITION_FAILED("Die Spracherkennung ist fehlgeschlagen. Bitte erneut starten."),
    TTS_UNAVAILABLE("Keine nutzbare Sprachausgabe für die Gerätesprache verfügbar. Bitte die Android-Sprachausgabe prüfen."),
    TTS_FAILED("Die Antwort konnte nicht vorgelesen werden. Gespeicherte Antworten stehen im Chatverlauf."),
    AUDIO_FOCUS("Ein anderer Audiovorgang hat Vorrang. Das Gespräch wurde beendet."),
    TIMEOUT("Der Sprachvorgang hat zu lange gedauert. Bitte erneut starten.")
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
    val status: String = "Bereit",
    val notice: String? = null
) { val active: Boolean get() = phase != VoicePhase.STOPPED }
