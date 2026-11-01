// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.voice

import android.os.Handler
import android.os.Looper
import com.azentrius.lautwerk.voice.AudioReleaseFence
import java.util.concurrent.Executors

/** Process-owned so a new service also waits for a previous service's host cleanup. */
internal object CarAudioResources {
    val control = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    val release = AudioReleaseFence(control, { main.post(it) })
}
