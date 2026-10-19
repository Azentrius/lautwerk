// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.text.Spanned
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.MessageTemplate
import androidx.car.app.testing.TestCarContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.azentrius.lautwerk.car.voice.VoicePreferences
import com.azentrius.lautwerk.car.LautwerkScreen
import com.azentrius.lautwerk.settings.ModelSelectionStore
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** App contract only: actual host contrast, clipping and quotas require the DHU. */
@RunWith(AndroidJUnit4::class)
class CarDesignContractTest {
    @Test fun packagedCarThemeContainsOnlyTheApprovedColorPairInBothModes() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        @Suppress("DEPRECATION")
        val info = target.packageManager.getApplicationInfo(target.packageName, PackageManager.GET_META_DATA)
        val style = info.metaData.getInt("androidx.car.app.theme")
        assertEquals(R.style.CarAppTheme, style)
        assertEquals(1, info.metaData.getInt("androidx.car.app.minCarApiLevel"))
        val attrs = intArrayOf(
            androidx.car.app.R.attr.carColorPrimary,
            androidx.car.app.R.attr.carColorPrimaryDark,
            androidx.car.app.R.attr.carColorSecondary,
            androidx.car.app.R.attr.carColorSecondaryDark
        )
        for (mode in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
            val config = Configuration(target.resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or mode
            }
            val context = target.createConfigurationContext(config)
            val theme = context.resources.newTheme().apply { applyStyle(style, true) }
            val values = theme.obtainStyledAttributes(attrs)
            try {
                assertEquals(Color.rgb(200,169,110), values.getColor(0, 0))
                assertEquals(Color.rgb(125,95,34), values.getColor(1, 0))
                assertFalse(values.hasValue(2))
                assertFalse(values.hasValue(3))
            } finally { values.recycle() }
        }
    }

    @Test fun templateKeepsItsSinglePlainActionAndUnchangedStructure() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val prefs = context.getSharedPreferences("car_design_contract", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            try {
                val template = LautwerkScreen(context, ModelSelectionStore(prefs), voicePreferences = VoicePreferences(prefs)).onGetTemplate() as MessageTemplate
                @Suppress("DEPRECATION")
                assertEquals(Action.APP_ICON, template.headerAction)
                @Suppress("DEPRECATION")
                assertNull(template.actionStrip)
                assertNull(template.icon)
                assertNull(template.debugMessage)
                assertFalse(template.isLoading)
                val action = template.actions.single()
                assertEquals(CarColor.PRIMARY, action.backgroundColor)
                assertNull(action.icon)
                assertEquals(0, action.flags)
                val title = action.title!!.toCharSequence()
                if (title is Spanned) assertEquals(0, title.getSpans(0, title.length, ForegroundCarColorSpan::class.java).size)
                assertNotNull(action.onClickDelegate)
            } finally { prefs.edit().clear().commit() }
        }
    }

    @Test fun unsupportedLocaleFallsBackToEnglishWithoutMixedTemplateText() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val config = Configuration(instrumentation.targetContext.resources.configuration).apply {
                setLocale(Locale.forLanguageTag("fr-FR"))
            }
            val localized = instrumentation.targetContext.createConfigurationContext(config)
            val context = TestCarContext.createCarContext(localized)
            val prefs = localized.getSharedPreferences("car_fallback_locale", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            try {
                val template = LautwerkScreen(context, ModelSelectionStore(prefs), voicePreferences = VoicePreferences(prefs)).onGetTemplate() as MessageTemplate
                @Suppress("DEPRECATION")
                assertEquals("Lautwerk", template.title.toString())
                assertEquals("Start audio check", template.actions.single().title.toString())
                assertEquals("Ready.\nChatGPT / OpenAI · GPT-5.4 mini", template.message.toString())
            } finally { prefs.edit().clear().commit() }
        }
    }
}
