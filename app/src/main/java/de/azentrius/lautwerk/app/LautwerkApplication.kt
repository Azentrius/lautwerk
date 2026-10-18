// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import android.app.Application
import de.azentrius.lautwerk.core.i18n.AppLanguage

class LautwerkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLanguage.preferences = {
            val locales = resources.configuration.locales
            (0 until locales.size()).map { locales[it] }
        }
    }
}
