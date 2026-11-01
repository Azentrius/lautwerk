// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.handoff

import android.content.Context
import android.content.pm.PackageManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import com.azentrius.lautwerk.car.controls.NativeAssistantPicker

object ExperimentScreenFactory {
    fun create(context: CarContext): Screen = if (nativeControlsEnabled(context))
        NativeAssistantPicker(context) else HandoffScreen(context)

    @Suppress("DEPRECATION")
    fun nativeControlsEnabled(context: Context): Boolean = HandoffExperiment.enabled(context) && runCatching {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData?.getBoolean("com.azentrius.lautwerk.car.NATIVE_CONTROLS", false) == true
    }.getOrDefault(false)
}
