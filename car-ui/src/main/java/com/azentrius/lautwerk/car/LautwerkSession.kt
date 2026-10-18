// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car

import android.content.Intent
import android.util.Log
import androidx.car.app.Screen
import androidx.car.app.Session

class LautwerkSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        Log.i(LautwerkCarService.TAG, "car.onCreateScreen api=${carContext.carAppApiLevel}")
        return LautwerkScreen(carContext)
    }
}
