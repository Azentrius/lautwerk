package dev.openautoai.car

import android.content.Intent
import android.util.Log
import androidx.car.app.Screen
import androidx.car.app.Session

class OpenAutoSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        Log.i(OpenAutoCarService.TAG, "car.onCreateScreen api=${carContext.carAppApiLevel}")
        return OpenAutoScreen(carContext)
    }
}
