// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.app.Activity
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import com.azentrius.lautwerk.car.voice.VoiceSetupActivity

object VoiceEntryPoint {
    fun attach(activity: Activity) {
        val original = activity.findViewById<Button>(R.id.start)
        val parent = original.parent as LinearLayout
        parent.addView(Button(activity).apply {
            setText(com.azentrius.lautwerk.car.R.string.voice_setup)
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(original.layoutParams as LinearLayout.LayoutParams)
            setOnClickListener { activity.startActivity(Intent(activity, VoiceSetupActivity::class.java)) }
        }, parent.indexOfChild(original) + 1)
    }
}
