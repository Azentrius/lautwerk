// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.handoff

import androidx.car.app.CarContext
import androidx.car.app.Screen

/** Native controls and their permission-bearing services do not exist in release builds. */
object ExperimentScreenFactory {
    fun create(context: CarContext): Screen = HandoffScreen(context)
}
