// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app.voice

import android.os.Handler
import com.azentrius.lautwerk.core.ai.CancellationSource
import com.azentrius.lautwerk.voice.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.FutureTask

internal class AndroidVoiceTasks(private val worker: ExecutorService, private val main: Handler) : VoiceTasks {
    override fun <T> run(work: (CancellationCheck) -> T, complete: (Result<T>) -> Unit): Cancel {
        val cancellation = CancellationSource()
        val task = FutureTask {
            val result = runCatching { cancellation.check(); work(cancellation) }
            main.post { if (!cancellation.isCancelled) complete(result) }
        }
        worker.execute(task)
        return Cancel { cancellation.cancel(); task.cancel(true) }
    }
}

internal class AndroidVoiceScheduler(private val main: Handler) : VoiceScheduler {
    override fun after(delayMillis: Long, action: () -> Unit): Cancel {
        val runnable = Runnable(action)
        main.postDelayed(runnable, delayMillis)
        return Cancel { main.removeCallbacks(runnable) }
    }
}
