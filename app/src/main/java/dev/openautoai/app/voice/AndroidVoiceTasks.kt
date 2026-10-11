package dev.openautoai.app.voice

import android.os.Handler
import dev.openautoai.core.ai.ChatError
import dev.openautoai.core.ai.ChatFailure
import dev.openautoai.voice.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicBoolean

internal class AndroidVoiceTasks(private val worker: ExecutorService, private val main: Handler) : VoiceTasks {
    override fun <T> run(work: (CancellationCheck) -> T, complete: (Result<T>) -> Unit): Cancel {
        val cancelled = AtomicBoolean(false)
        val check = CancellationCheck {
            if (cancelled.get() || Thread.currentThread().isInterrupted) throw ChatFailure(ChatError.CANCELLED)
        }
        val task = FutureTask {
            val result = runCatching { check.check(); work(check) }
            main.post { if (!cancelled.get()) complete(result) }
        }
        worker.execute(task)
        return Cancel { cancelled.set(true); task.cancel(true) }
    }
}

internal class AndroidVoiceScheduler(private val main: Handler) : VoiceScheduler {
    override fun after(delayMillis: Long, action: () -> Unit): Cancel {
        val runnable = Runnable(action)
        main.postDelayed(runnable, delayMillis)
        return Cancel { main.removeCallbacks(runnable) }
    }
}
