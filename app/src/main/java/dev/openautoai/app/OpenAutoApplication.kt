package dev.openautoai.app

import android.app.Application
import dev.openautoai.core.i18n.AppLanguage

class OpenAutoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLanguage.preferences = {
            val locales = resources.configuration.locales
            (0 until locales.size()).map { locales[it] }
        }
    }
}
