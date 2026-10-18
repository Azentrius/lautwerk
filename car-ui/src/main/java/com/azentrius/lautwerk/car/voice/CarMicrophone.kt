// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.car.app.CarContext
import androidx.annotation.RequiresApi
import androidx.car.app.media.CarAudioRecord
import com.azentrius.lautwerk.voice.Cancel
import com.azentrius.lautwerk.voice.VoiceProblem
import com.azentrius.lautwerk.voice.SerialAudioCapture
import java.util.concurrent.ExecutorService

/** Streams host PCM to a recognizer pipe. No raw audio is written to storage. Car API 5+. */
@RequiresApi(33)
internal class CarMicrophone(private val context: CarContext, private val main: Handler,
    private val worker: ExecutorService, private val control: ExecutorService) {
    fun stream(output: ParcelFileDescriptor, failed: (VoiceProblem) -> Unit): Cancel {
        var capture: Cancel? = null
        val focus = VoiceFocus(context, main, capture = true).request {
            failed(VoiceProblem.AUDIO_FOCUS)
        } ?: throw AudioFailure(VoiceProblem.AUDIO_FOCUS)
        val cleanup = Cancel {
            // Invalidate reads first; neither the host's start nor stop IPC can block this caller.
            capture?.cancel()
            runCatching { output.close() }
            focus.cancel()
        }
        try {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                throw AudioFailure(VoiceProblem.PERMISSION)
            if (context.carAppApiLevel < 5) throw AudioFailure(VoiceProblem.RECOGNIZER_UNAVAILABLE)
            Os.fcntlInt(output.fileDescriptor, OsConstants.F_SETFL, OsConstants.O_NONBLOCK)
            capture = SerialAudioCapture(control, worker).start(create = {
                val recorder = CarAudioRecord.create(context)
                object : SerialAudioCapture.Device {
                    override fun start() {
                        recorder.startRecording()
                        Log.i(VoiceFocus.TAG, "voice.car.capture.start rate=16000 channels=1 pcm=16")
                    }
                    override fun read(cancelled: () -> Boolean) {
                        val buffer = ByteArray(CarAudioRecord.AUDIO_CONTENT_BUFFER_SIZE)
                        var bytes = 0L
                        var peak = 0
                        while (!cancelled()) {
                            val size = recorder.read(buffer, 0, buffer.size)
                            if (size < 0) {
                                // Host dismissal must not submit a partial question to the provider.
                                Log.i(VoiceFocus.TAG, "voice.car.capture.hostStopped")
                                throw AudioFailure(VoiceProblem.CAPTURE_STOPPED)
                            }
                            if (size == 0) { Thread.sleep(10); continue }
                            for (i in 0 until size - 1 step 2) {
                                val sample = ((buffer[i].toInt() and 255) or (buffer[i + 1].toInt() shl 8)).toShort().toInt()
                                peak = maxOf(peak, kotlin.math.abs(sample))
                            }
                            bytes += size
                            if (bytes == size.toLong()) Log.i(VoiceFocus.TAG, "voice.car.capture.firstFrame")
                            var offset = 0
                            while (offset < size && !cancelled()) {
                                try { offset += Os.write(output.fileDescriptor, buffer, offset, size - offset) }
                                catch (error: ErrnoException) {
                                    if (error.errno != OsConstants.EAGAIN) throw error
                                    Thread.sleep(10)
                                }
                            }
                        }
                        Log.i(VoiceFocus.TAG, "voice.car.capture.end bytes=$bytes signal=${peak > 0}")
                    }
                    override fun stop() {
                        val started = SystemClock.elapsedRealtime()
                        try { recorder.stopRecording() }
                        finally { Log.i(VoiceFocus.TAG, "voice.car.capture.stopped elapsedMs=${SystemClock.elapsedRealtime() - started}") }
                    }
                }
            }, failed = { error ->
                main.post { failed((error as? AudioFailure)?.problem ?: VoiceProblem.RECOGNITION_FAILED) }
            })
        } catch (error: Exception) { cleanup.cancel(); throw error }
        return cleanup
    }
}

internal class AudioFailure(val problem: VoiceProblem) : Exception()
