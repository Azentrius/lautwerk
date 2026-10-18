// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.car.voice

import android.os.Handler
import de.azentrius.lautwerk.core.ai.CancellationSource
import de.azentrius.lautwerk.voice.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.FutureTask

internal class VoiceWorkers(private val worker: ExecutorService, private val main: Handler) : VoiceTasks {
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

internal class VoiceTimer(private val main: Handler) : VoiceScheduler {
    override fun after(delayMillis: Long, action: () -> Unit): Cancel {
        val job = Runnable(action)
        main.postDelayed(job, delayMillis)
        return Cancel { main.removeCallbacks(job) }
    }
}
