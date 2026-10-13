// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.prototype

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.test.core.app.ActivityScenario
import dev.openautoai.car.voice.VoiceSetupActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks only the setup surface; does not enter keys, grant permissions or start audio. */
class CarPhoneDesignTest {
    @Test fun setupPageUsesTheRuntimePaletteInsteadOfTheInkLaunchWindow() {
        ActivityScenario.launch(VoiceSetupActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val page = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                assertTrue(page is ScrollView)
                assertEquals(activity.getColor(R.color.az_bg_page), (page.background as ColorDrawable).color)
                assertTrue("Secure key entry must remain protected",
                    activity.window.attributes.flags and android.view.WindowManager.LayoutParams.FLAG_SECURE != 0)
            }
        }
    }
}
