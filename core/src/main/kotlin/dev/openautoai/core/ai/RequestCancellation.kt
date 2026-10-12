package dev.openautoai.core.ai

/** Per-operation cancellation, shared from the worker down to its single HTTP call. */
fun interface RequestCancellation {
    fun check()
    /** Handlers must be quick, thread-safe and must not wait for the worker. */
    fun onCancel(action: () -> Unit): AutoCloseable { check(); return AutoCloseable {} }

    companion object {
        val NONE = RequestCancellation {
            if (Thread.currentThread().isInterrupted) throw ChatFailure(ChatError.CANCELLED)
        }
    }
}

class CancellationSource : RequestCancellation {
    private val lock = Any()
    @Volatile var isCancelled = false
        private set
    private val handlers = mutableMapOf<Any, () -> Unit>()

    override fun check() {
        if (isCancelled || Thread.currentThread().isInterrupted) throw ChatFailure(ChatError.CANCELLED)
    }

    override fun onCancel(action: () -> Unit): AutoCloseable {
        val key = Any()
        val alreadyCancelled = synchronized(lock) {
            if (isCancelled) true else { handlers[key] = action; false }
        }
        if (alreadyCancelled) runCatching(action)
        return AutoCloseable { synchronized(lock) { handlers.remove(key) }; Unit }
    }

    fun cancel() {
        val pending = synchronized(lock) {
            if (isCancelled) return
            isCancelled = true
            handlers.values.toList().also { handlers.clear() }
        }
        // Never invoke external code while holding the registration lock.
        pending.forEach { runCatching(it) }
    }
}
