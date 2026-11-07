// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

import org.junit.Assert.*
import org.junit.Test

class ControlLeaseStoreTest {
    @Test fun consumesOnlyOnceAndUpdatesDoNotRearm() {
        val store = ControlLeaseStore<String>()
        store.update("fixture", 0, 1, "action")
        val ticket = store.ticket(0)!!
        assertEquals("action", store.consume(ticket))
        assertNull(store.consume(ticket))
        store.update("fixture", 0, 2, "new action")
        assertNull(store.ticket(0))
        assertTrue(store.hasRecords)
        store.remove("fixture")
        store.update("fixture", 0, 3, "restart action")
        assertEquals("restart action", store.consume(store.ticket(0)!!))
        assertNull(store.consume(ticket))
    }
    @Test fun replacedRemovedAndDisconnectedTicketsCannotBeSent() {
        val store = ControlLeaseStore<String>()
        for (mode in 0..2) {
            store.update("fixture", 0, 1, "action")
            val ticket = store.ticket(0)!!
            when (mode) {
                0 -> store.update("fixture", 0, 2, "replacement")
                1 -> store.remove("fixture")
                2 -> store.clear()
            }
            assertNull(store.consume(ticket))
            store.clear()
        }
    }
    @Test fun ambiguityIsScopedToTargetAndSnapshotRemovalInvalidates() {
        val store = ControlLeaseStore<String>()
        store.update("one", 0, 1, "first")
        val stale = store.ticket(0)!!
        store.update("two", 0, 1, "second")
        store.update("other", 1, 1, "other")
        assertNull(store.ticket(0))
        assertNull(store.consume(stale))
        assertEquals("other", store.consume(store.ticket(1)!!))
        store.retain(setOf("two"))
        assertEquals("second", store.consume(store.ticket(0)!!))
        assertNull(store.consume(stale))
    }
}
