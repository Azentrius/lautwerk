// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car.voice

import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi

/** Shared with the opt-in PCM device probe so its configuration matches the production car path. */
object ExternalSpeechInput {
    @RequiresApi(33)
    fun attach(intent: Intent, input: ParcelFileDescriptor): Intent = intent
        .putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, input)
        .putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, RecognizerIntent.EXTRA_AUDIO_SOURCE)
        .putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1)
        .putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
        .putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, 16000)
}

/** Both final callbacks are terminal for a single question; late/duplicate callbacks are ignored. */
open class FinalSpeechListener(
    private val recognized: (String) -> Unit,
    private val failed: (Int) -> Unit,
    private val endOfSpeech: () -> Unit = {}
) : RecognitionListener {
    private var delivered = false
    override fun onResults(results: Bundle?) {
        if (delivered) return
        delivered = true
        recognized(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty())
    }
    override fun onSegmentResults(segmentResults: Bundle) = onResults(segmentResults)
    override fun onEndOfSegmentedSession() { if (!delivered) onError(SpeechRecognizer.ERROR_NO_MATCH) }
    override fun onError(error: Int) { if (!delivered) { delivered = true; failed(error) } }
    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = endOfSpeech()
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
