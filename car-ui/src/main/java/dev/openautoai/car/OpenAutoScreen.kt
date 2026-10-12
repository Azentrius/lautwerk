// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import dev.openautoai.core.PrototypeSession
import dev.openautoai.settings.ModelSelectionStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.Closeable

class OpenAutoScreen(
    carContext: CarContext,
    private val selections: ModelSelectionStore = ModelSelectionStore(carContext)
) : Screen(carContext) {
    private val session = PrototypeSession()
    private var subscription: Closeable? = null

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                subscription = selections.observe { invalidate() }
            }
            override fun onStop(owner: LifecycleOwner) {
                subscription?.close()
                subscription = null
            }
        })
    }

    @Suppress("DEPRECATION") // API-1 title/header remains usable on older hosts.
    override fun onGetTemplate(): Template {
        Log.i(OpenAutoCarService.TAG, "car.onGetTemplate count=${session.startCount}")
        val message = if (session.startCount == 0) carContext.getString(R.string.car_ready)
            else carContext.getString(R.string.car_started, session.startCount)
        val selected = selections.load()
        val content = carContext.getString(R.string.car_selection, message,
            selected.provider.displayName, selected.model.label)
        return MessageTemplate.Builder(content)
            .setTitle(carContext.getString(R.string.car_title))
            .setHeaderAction(Action.APP_ICON)
            .addAction(Action.Builder()
                .setTitle(carContext.getString(R.string.car_start))
                .setBackgroundColor(CarColor.PRIMARY)
                .setOnClickListener {
                    session.start()
                    Log.i(OpenAutoCarService.TAG, "car.start count=${session.startCount}")
                    invalidate()
                }.build())
            .build()
    }
}
