// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.prototype

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.car.voice.VoiceSetupActivity
import dev.openautoai.car.voice.ExternalSpeechInput
import dev.openautoai.car.voice.FinalSpeechListener
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Opt-in hardware capability probe. Synthesizes a fixed fixture, never sends an AI request. */
class ExternalPcmRecognitionTest {
    @Test fun localRecognizerConsumesInjectedPcmInsteadOfThePhoneMicrophone() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("externalPcm") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        assertTrue(Build.VERSION.SDK_INT >= 33)
        assertEquals(PackageManager.PERMISSION_GRANTED, context.checkSelfPermission(Manifest.permission.RECORD_AUDIO))
        assertTrue(SpeechRecognizer.isOnDeviceRecognitionAvailable(context))
        val wav = File.createTempFile("synthetic-pcm-", ".wav", context.cacheDir)
        var engine: TextToSpeech? = null
        var recognizer: SpeechRecognizer? = null
        var pipe: Array<ParcelFileDescriptor>? = null
        ActivityScenario.launch(VoiceSetupActivity::class.java).use {
            try {
                val initialized = CountDownLatch(1)
                var initialization = -1
                instrumentation.runOnMainSync { engine = TextToSpeech(context) { status -> initialization = status; initialized.countDown() } }
                assertTrue("TTS initialization timed out", initialized.await(15, TimeUnit.SECONDS))
                assertEquals(TextToSpeech.SUCCESS, initialization)
                val synthesized = CountDownLatch(1)
                var synthesisOk = false
                var synthesisError = 0
                instrumentation.runOnMainSync {
                    assertTrue(engine!!.setLanguage(Locale.GERMANY) >= TextToSpeech.LANG_AVAILABLE)
                    val available = engine!!.voices.filter { !it.isNetworkConnectionRequired && it.locale.language == "de" &&
                        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
                    val local = engine!!.voice?.takeIf { it in available } ?: available.firstOrNull()
                    assertNotNull("Install a German offline TTS voice", local)
                    engine!!.voice = local
                    engine!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) = Unit
                        override fun onDone(id: String?) { synthesisOk = true; synthesized.countDown() }
                        @Deprecated("Android listener") override fun onError(id: String?) { synthesized.countDown() }
                        override fun onError(id: String?, code: Int) { synthesisError = code; synthesized.countDown() }
                    })
                    assertEquals(TextToSpeech.SUCCESS, engine!!.synthesizeToFile(
                        "Die blaue Lampe steht neben dem Fenster.", Bundle(), wav, "synthetic-input"))
                }
                assertTrue("TTS fixture timed out", synthesized.await(20, TimeUnit.SECONDS))
                assertEquals("TTS fixture error", 0, synthesisError)
                assertTrue("TTS fixture failed", synthesisOk)
                val pcm = mono16k(wav.readBytes())
                assertTrue("Synthetic PCM must contain audio", pcm.any { it != 0.toByte() })
                if (InstrumentationRegistry.getArguments().getString("keepSyntheticPcm") == "true")
                    File(context.cacheDir, "car-voice-synthetic.pcm").writeBytes(ByteArray(16000) + pcm + ByteArray(64000))
                val ready = CountDownLatch(1)
                val finished = CountDownLatch(1)
                var matched = false
                var matchingWords = 0
                var resultLength = 0
                var bytesWritten = 0
                var errorCode = 0
                pipe = ParcelFileDescriptor.createPipe()
                instrumentation.runOnMainSync {
                    recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context).apply {
                        setRecognitionListener(object : FinalSpeechListener(recognized = { text ->
                                // No transcript in assertions or logs, including unexpected phone-mic input.
                                val result = text.lowercase(Locale.ROOT)
                                matchingWords = listOf("blaue", "lampe", "steht", "neben", "fenster").count { result.contains(it) }
                                resultLength = result.length
                                matched = matchingWords >= 3
                                finished.countDown()
                            }, failed = { error -> errorCode = error; ready.countDown(); finished.countDown() }, segmented = true) {
                            override fun onReadyForSpeech(params: Bundle?) { ready.countDown() }
                        })
                        startListening(ExternalSpeechInput.attach(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE"), pipe!![0]))
                    }
                }
                assertTrue("Recognizer initialization timed out", ready.await(10, TimeUnit.SECONDS))
                assertEquals("Recognizer rejected external input", 0, errorCode)
                // Closing/destroying the reader in finally unblocks this bounded writer if unsupported.
                val writer = Thread {
                    runCatching {
                        ParcelFileDescriptor.AutoCloseOutputStream(pipe!![1]).use { stream ->
                            // Four seconds of silence must not trigger a premature empty final result.
                            val fixture = ByteArray(128000) + pcm + ByteArray(96000)
                            var offset = 0
                            while (offset < fixture.size && finished.count > 0) {
                                val size = minOf(512, fixture.size - offset)
                                stream.write(fixture, offset, size); offset += size; bytesWritten = offset
                                Thread.sleep(16)
                            }
                        }
                    }
                }.apply { isDaemon = true; start() }
                assertTrue("External PCM recognition timed out", finished.await(25, TimeUnit.SECONDS))
                assertEquals("External PCM recognition error", 0, errorCode)
                assertTrue("Synthetic fixture not recognized: matches=$matchingWords length=$resultLength pcmBytes=${pcm.size} sent=$bytesWritten", matched)
                writer.join(1000)
            } finally {
                instrumentation.runOnMainSync { recognizer?.cancel(); recognizer?.destroy(); engine?.shutdown() }
                pipe?.forEach { runCatching { it.close() } }
                wav.delete()
            }
        }
    }

    private fun mono16k(wav: ByteArray): ByteArray {
        val data = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals("RIFF", String(wav, 0, 4, Charsets.US_ASCII))
        var position = 12
        var rate = 0
        var samples = ShortArray(0)
        while (position + 8 <= wav.size) {
            val id = String(wav, position, 4, Charsets.US_ASCII)
            val size = data.getInt(position + 4)
            assertTrue(size >= 0 && position + 8L + size <= wav.size)
            if (id == "fmt ") {
                assertEquals(1, data.getShort(position + 8).toInt())
                assertEquals(1, data.getShort(position + 10).toInt())
                rate = data.getInt(position + 12)
                assertEquals(16, data.getShort(position + 22).toInt())
            }
            if (id == "data") samples = ShortArray(size / 2) { data.getShort(position + 8 + it * 2) }
            position += 8 + size + size % 2
        }
        assertTrue(rate > 0 && samples.isNotEmpty())
        val converted = ByteBuffer.allocate(samples.size * 16000 / rate * 2).order(ByteOrder.LITTLE_ENDIAN)
        repeat(converted.capacity() / 2) { i -> converted.putShort(samples[(i.toLong() * rate / 16000).toInt()]) }
        return converted.array()
    }
}
