// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.voice

/** Main-thread, in-memory leases for explicit controls. A lease is not a conversation state. */
class ControlLeaseStore<T> {
    data class Ticket(val key: String, val target: Int, val revision: Long)
    private data class Record<T>(val ticket: Ticket, val stamp: Long, val value: T)
    private val records = mutableMapOf<String, Record<T>>()
    private val consumed = mutableSetOf<String>()
    private var revision = 0L

    val hasRecords: Boolean get() = records.isNotEmpty()

    fun update(key: String, target: Int, stamp: Long, value: T) {
        val old = records[key]
        if (old != null && old.ticket.target == target && old.stamp == stamp && old.value == value) return
        records[key] = Record(Ticket(key, target, ++revision), stamp, value)
        // A notification update never rearms an already dispatched action. Removal is required.
    }

    fun ticket(target: Int): Ticket? = records.values.filter { it.ticket.target == target }
        .singleOrNull()?.ticket?.takeUnless { it.key in consumed }

    fun peek(ticket: Ticket): T? = records[ticket.key]?.takeIf {
        it.ticket == ticket && ticket.key !in consumed && this.ticket(ticket.target) == ticket
    }?.value

    fun consume(ticket: Ticket): T? = peek(ticket)?.also { consumed.add(ticket.key) }
    fun remove(key: String) { records.remove(key); consumed.remove(key) }
    fun retain(keys: Set<String>) { (records.keys - keys).forEach(::remove) }
    fun clear() { records.clear(); consumed.clear() } // Revision never resets, including reconnect.
}
