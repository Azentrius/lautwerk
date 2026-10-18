// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.core.ai

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CancellationSourceTest {
    @Test fun cancellationIsIdempotentAndAlsoReachesLateRegistrations() {
        val source = CancellationSource()
        var calls = 0
        source.onCancel { calls++ }
        source.cancel(); source.cancel()
        source.onCancel { calls++ }
        assertEquals(2, calls)
        assertEquals(ChatError.CANCELLED, assertThrows(ChatFailure::class.java) { source.check() }.kind)
    }

    @Test fun disposedHandlersAndOtherRequestsAreUnaffectedByOldCancellation() {
        val old = CancellationSource()
        val fresh = CancellationSource()
        old.onCancel { fail("disposed") }.close()
        fresh.onCancel { fail("different request") }
        old.onCancel { throw IllegalStateException("cleanup failed") }
        var cleaned = false
        old.onCancel { cleaned = true }
        old.cancel()
        fresh.check()
        assertTrue(cleaned)
        assertFalse(fresh.isCancelled)
    }

    @Test fun registrationRacingCancellationCannotLoseOrDuplicateTheCallback() {
        val workers = Executors.newFixedThreadPool(2)
        try {
            repeat(100) {
                val source = CancellationSource()
                val barrier = CyclicBarrier(2)
                val calls = AtomicInteger()
                val a = workers.submit { barrier.await(); source.onCancel { calls.incrementAndGet() } }
                val b = workers.submit { barrier.await(); source.cancel() }
                a.get(3, TimeUnit.SECONDS); b.get(3, TimeUnit.SECONDS)
                assertEquals(1, calls.get())
            }
        } finally { workers.shutdownNow() }
    }
}
