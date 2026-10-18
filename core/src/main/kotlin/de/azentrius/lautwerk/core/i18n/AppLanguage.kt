// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.core.i18n

import java.util.Locale

/** Android installs its current preference list; JVM callers use the process locale. */
object AppLanguage {
    @Volatile var preferences: () -> List<Locale> = { listOf(Locale.getDefault()) }
    fun resolve(locales: List<Locale>): Locale = locales.firstOrNull { it.language in setOf("de", "en") } ?: Locale.ENGLISH
    fun locale(): Locale = resolve(preferences())
}
