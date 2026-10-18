// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Locale

/** Instrumentation host only. Cannot open stores, make requests or launch a voice service. */
class LifecycleProbeActivity : ComponentActivity() {
    internal lateinit var home: HomeViewModel
    @android.annotation.SuppressLint("AppBundleLocaleChanges") // Debug test host; no language-split bundle.
    override fun attachBaseContext(base: Context) {
        val config=Configuration(base.resources.configuration).apply { setLocale(LifecycleProbeActivity.locale); fontScale=LifecycleProbeActivity.fontScale; uiMode=(uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or if(LifecycleProbeActivity.dark)Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO }
        super.attachBaseContext(base.createConfigurationContext(config))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val create=factory ?: run { finish(); return }
        home=ViewModelProvider(this,object:ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T:ViewModel> create(modelClass:Class<T>):T = create() as T
        })[HomeViewModel::class.java]
        home.languageChanged()
        setContent { de.azentrius.lautwerk.app.design.AzentriusTheme(dark=dark) { MainContent(home,{ starts++ },{
            AppState.update(false,AppState.provider,AppState.modelId,AppState.sessionId,"Test")
        }) } }
    }
    override fun onStop() {
        if (::home.isInitialized && !isChangingConfigurations) {
            home.cancelStart(); home.cancelExportPreparation()
        }
        super.onStop()
    }
    companion object {
        internal var factory:(()->HomeViewModel)?=null
        internal var locale:Locale=Locale.GERMAN
        internal var starts=0
        internal var dark=false
        internal var fontScale=1f
    }
}
