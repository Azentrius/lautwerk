// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import android.Manifest
import android.content.pm.PackageManager
import dev.openautoai.car.voice.*
import dev.openautoai.settings.ModelSelectionStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.Closeable

class OpenAutoScreen(
    carContext: CarContext,
    private val selections: ModelSelectionStore = ModelSelectionStore(carContext),
    private val voice: CarVoicePort = VoiceConnection(carContext, carContext)
) : Screen(carContext) {
    private var subscription: Closeable? = null
    private var visible = false
    private var requestingPermission = false
    private var permissionIssue = false
    private var permissionGeneration = 0L

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                visible = true
                subscription = selections.observe { invalidate() }
                voice.connect { invalidate() }
            }
            override fun onStop(owner: LifecycleOwner) {
                visible = false
                permissionGeneration++
                requestingPermission = false
                subscription?.close()
                subscription = null
                voice.close()
            }
        })
    }

    @Suppress("DEPRECATION") // API-1 title/header remains usable on older hosts.
    override fun onGetTemplate(): Template {
        val endsSession = voice.state.active
        Log.i(OpenAutoCarService.TAG, "car.onGetTemplate active=${voice.state.active}")
        val message = if (permissionIssue) carContext.getString(R.string.car_permission)
            else voice.state.summary.ifBlank { carContext.getString(R.string.car_ready) }
        val selected = voice.state.selection ?: selections.load()
        val content = carContext.getString(R.string.car_selection, message,
            selected.provider.displayName, selected.model.label)
        return MessageTemplate.Builder(content)
            .setTitle(carContext.getString(R.string.car_title))
            .setHeaderAction(Action.APP_ICON)
            .addAction(Action.Builder()
                .setTitle(carContext.getString(if (endsSession) R.string.voice_stop else R.string.car_start))
                .setBackgroundColor(CarColor.PRIMARY)
                .setOnClickListener {
                    // The host can deliver a click from an older template. Preserve its intent.
                    Log.i(OpenAutoCarService.TAG, "car.voice.action=${if (endsSession) "stop" else "start"}")
                    if (endsSession) {
                        permissionGeneration++
                        requestingPermission = false
                        voice.stop()
                    } else start()
                }.build())
            .build()
    }

    private fun start() {
        if (requestingPermission || voice.state.active) return
        permissionIssue = false
        if (!voice.state.active && VoicePreferences(carContext).mode != VoiceMode.PLAYBACK &&
            carContext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestingPermission = true
            val token = ++permissionGeneration
            try {
                carContext.requestPermissions(listOf(Manifest.permission.RECORD_AUDIO), carContext.mainExecutor) { approved, _ ->
                    if (token != permissionGeneration) return@requestPermissions
                    requestingPermission = false
                    permissionIssue = Manifest.permission.RECORD_AUDIO !in approved
                    if (visible && Manifest.permission.RECORD_AUDIO in approved) voice.start()
                    invalidate()
                }
            } catch (_: Exception) { requestingPermission = false; permissionIssue = true; invalidate() }
        } else voice.start()
    }
}
