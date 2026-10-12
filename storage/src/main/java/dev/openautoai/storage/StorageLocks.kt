// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.storage

import java.util.concurrent.ConcurrentHashMap

/** The app has one process. These locks are shared by Activity, Service and repository instances. */
internal object StorageLocks {
    private val locks = ConcurrentHashMap<String, Any>()
    fun forName(name: String): Any = locks.computeIfAbsent(name) { Any() }
}
