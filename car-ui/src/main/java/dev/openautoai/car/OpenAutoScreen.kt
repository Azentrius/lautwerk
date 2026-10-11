package dev.openautoai.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import dev.openautoai.core.PrototypeSession

class OpenAutoScreen(carContext: CarContext) : Screen(carContext) {
    private val session = PrototypeSession()

    @Suppress("DEPRECATION") // API-1 title/header remains usable on older hosts.
    override fun onGetTemplate(): Template {
        Log.i(OpenAutoCarService.TAG, "car.onGetTemplate count=${session.startCount}")
        val message = if (session.startCount == 0) carContext.getString(R.string.car_ready)
            else carContext.getString(R.string.car_started, session.startCount)
        return MessageTemplate.Builder(message)
            .setTitle(carContext.getString(R.string.car_title))
            .setHeaderAction(Action.APP_ICON)
            .addAction(Action.Builder()
                .setTitle(carContext.getString(R.string.car_start))
                .setOnClickListener {
                    session.start()
                    Log.i(OpenAutoCarService.TAG, "car.start count=${session.startCount}")
                    invalidate()
                }.build())
            .build()
    }
}
