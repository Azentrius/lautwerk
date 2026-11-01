// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import android.Manifest
import android.content.pm.PackageManager
import com.azentrius.lautwerk.car.voice.*
import com.azentrius.lautwerk.settings.ModelSelectionStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.Closeable
import com.azentrius.lautwerk.car.handoff.ExperimentScreenFactory
import androidx.car.app.model.ParkedOnlyOnClickListener

class LautwerkScreen(
    carContext: CarContext,
    private val selections: ModelSelectionStore = ModelSelectionStore(carContext),
    private val voice: CarVoicePort = VoiceConnection(carContext, carContext),
    private val voicePreferences: VoicePreferences = VoicePreferences(carContext),
    private val handoffExperiment: Boolean = false
) : Screen(carContext) {
    private var subscription: Closeable? = null
    private var modeSubscription: Closeable? = null
    private var visible = false
    private var requestingPermission = false
    private var permissionIssue = false
    private var permissionGeneration = 0L

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                visible = true
                subscription = selections.observe { invalidate() }
                modeSubscription = voicePreferences.observeMode {
                    // An active session still offers End; redraw its normal state changes only.
                    if (!voice.state.active) invalidate()
                }
                voice.connect { invalidate() }
            }
            override fun onStop(owner: LifecycleOwner) {
                visible = false
                permissionGeneration++
                requestingPermission = false
                subscription?.close()
                subscription = null
                modeSubscription?.close()
                modeSubscription = null
                voice.close()
            }
        })
    }

    @Suppress("DEPRECATION") // API-1 title/header remains usable on older hosts.
    override fun onGetTemplate(): Template {
        val endsSession = voice.state.active
        val actionTitle = when {
            endsSession -> R.string.voice_stop
            voicePreferences.mode == VoiceMode.CONVERSATION -> R.string.car_start_conversation
            else -> R.string.car_start_audio_check
        }
        Log.i(LautwerkCarService.TAG, "car.onGetTemplate active=${voice.state.active}")
        val message = if (permissionIssue) carContext.getString(R.string.car_permission)
            else voice.state.summary.ifBlank { carContext.getString(R.string.car_ready) }
        val selected = voice.state.selection ?: selections.load()
        val content = carContext.getString(R.string.car_selection, message,
            selected.provider.displayName, selected.model.label)
        val builder = MessageTemplate.Builder(content)
            .setTitle(carContext.getString(R.string.car_title))
            .setHeaderAction(Action.APP_ICON)
            .addAction(Action.Builder()
                .setTitle(carContext.getString(actionTitle))
                .setBackgroundColor(CarColor.PRIMARY)
                .setOnClickListener {
                    // The host can deliver a click from an older template. Preserve its intent.
                    Log.i(LautwerkCarService.TAG, "car.voice.action=${if (endsSession) "stop" else "start"}")
                    if (endsSession) {
                        permissionGeneration++
                        requestingPermission = false
                        voice.stop()
                    } else start()
                }.build())
        if (handoffExperiment) {
            builder.addAction(Action.Builder().setTitle(carContext.getString(R.string.handoff_open))
                .setBackgroundColor(CarColor.PRIMARY)
                .setOnClickListener(ParkedOnlyOnClickListener.create {
                    if (visible) {
                        permissionGeneration++
                        requestingPermission = false
                        screenManager.push(ExperimentScreenFactory.create(carContext))
                    }
                }).build())
        }
        return builder.build()
    }

    private fun start() {
        if (requestingPermission || voice.state.active) return
        permissionIssue = false
        if (!voice.state.active && voicePreferences.mode != VoiceMode.PLAYBACK &&
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
