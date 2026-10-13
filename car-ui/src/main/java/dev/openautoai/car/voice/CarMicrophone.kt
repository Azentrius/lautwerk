// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.car.app.CarContext
import androidx.annotation.RequiresApi
import androidx.car.app.media.CarAudioRecord
import dev.openautoai.voice.Cancel
import dev.openautoai.voice.VoiceProblem
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

/** Streams host PCM to a recognizer pipe. No raw audio is written to storage. Car API 5+. */
@RequiresApi(33)
internal class CarMicrophone(private val context: CarContext, private val main: Handler, private val worker: ExecutorService) {
    fun stream(output: ParcelFileDescriptor, failed: (VoiceProblem) -> Unit): Cancel {
        val cancelled = AtomicBoolean(false)
        val lock = Any()
        var recorder: CarAudioRecord? = null
        val focus = VoiceFocus(context, main, capture = true).request {
            failed(VoiceProblem.AUDIO_FOCUS)
        } ?: throw AudioFailure(VoiceProblem.AUDIO_FOCUS)
        val cleanup = Cancel {
            if (cancelled.compareAndSet(false, true)) {
                // Closing the host stream unblocks a pending read. Writes are non-blocking below.
                synchronized(lock) { runCatching { recorder?.stopRecording() }; recorder = null }
                runCatching { output.close() }
                focus.cancel()
            }
        }
        try {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                throw AudioFailure(VoiceProblem.PERMISSION)
            if (context.carAppApiLevel < 5) throw AudioFailure(VoiceProblem.RECOGNIZER_UNAVAILABLE)
            Os.fcntlInt(output.fileDescriptor, OsConstants.F_SETFL, OsConstants.O_NONBLOCK)
            worker.execute {
                try {
                    synchronized(lock) {
                        if (cancelled.get()) return@execute
                        recorder = CarAudioRecord.create(context).also { it.startRecording() }
                    }
                    Log.i(VoiceFocus.TAG, "voice.car.capture.start rate=16000 channels=1 pcm=16")
                    val buffer = ByteArray(CarAudioRecord.AUDIO_CONTENT_BUFFER_SIZE)
                    var bytes = 0L
                    var peak = 0
                    while (!cancelled.get()) {
                        val current = synchronized(lock) { recorder } ?: break
                        val size = current.read(buffer, 0, buffer.size)
                        if (size < 0) {
                            // Host dismissal must not submit a partial question to the provider.
                            main.post { if (!cancelled.get()) failed(VoiceProblem.NO_INPUT) }
                            break
                        }
                        if (size == 0) { Thread.sleep(10); continue }
                        for (i in 0 until size - 1 step 2) {
                            val sample = ((buffer[i].toInt() and 255) or (buffer[i + 1].toInt() shl 8)).toShort().toInt()
                            peak = maxOf(peak, kotlin.math.abs(sample))
                        }
                        bytes += size
                        if (bytes == size.toLong()) Log.i(VoiceFocus.TAG, "voice.car.capture.firstFrame")
                        var offset = 0
                        while (offset < size && !cancelled.get()) {
                            try { offset += Os.write(output.fileDescriptor, buffer, offset, size - offset) }
                            catch (error: ErrnoException) {
                                if (error.errno != OsConstants.EAGAIN) throw error
                                Thread.sleep(10)
                            }
                        }
                    }
                    Log.i(VoiceFocus.TAG, "voice.car.capture.end bytes=$bytes signal=${peak > 0}")
                } catch (_: Exception) {
                    main.post { if (!cancelled.get()) failed(VoiceProblem.RECOGNITION_FAILED) }
                }
            }
        } catch (error: Exception) { cleanup.cancel(); throw error }
        return cleanup
    }
}

internal class AudioFailure(val problem: VoiceProblem) : Exception()
