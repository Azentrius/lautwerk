// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.core

/** Local UI probe only. Each phone activity/car session owns its own state. */
class PrototypeSession(initialStarts: Int = 0) {
    var startCount: Int = initialStarts.coerceAtLeast(0)
        private set

    fun start() { startCount++ }
}
