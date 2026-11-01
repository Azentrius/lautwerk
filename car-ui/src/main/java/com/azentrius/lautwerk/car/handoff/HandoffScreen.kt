// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.handoff

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.azentrius.lautwerk.car.LautwerkCarService
import com.azentrius.lautwerk.car.R
import com.azentrius.lautwerk.car.voice.CarVoicePort
import com.azentrius.lautwerk.car.voice.VoiceConnection
import com.azentrius.lautwerk.car.voice.VoiceTimer
import com.azentrius.lautwerk.voice.AudioHandoff
import com.azentrius.lautwerk.voice.HandoffPhase

/** Debug opt-in screen. No recognition, provider request or external session monitoring. */
class HandoffScreen(
    carContext: CarContext,
    private val voice: CarVoicePort = VoiceConnection(carContext, carContext),
    private val launcher: AssistantLauncher = AndroidAssistantLauncher(carContext)
) : Screen(carContext) {
    private var visible = false
    private val handoff = AudioHandoff(voice::releaseForHandoff, VoiceTimer(Handler(Looper.getMainLooper()))) {
        Log.i(LautwerkCarService.TAG, "handoff.phase=$it")
        if (visible) invalidate()
    }

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                visible = true
                voice.connect { if (visible) invalidate() }
            }
            override fun onStop(owner: LifecycleOwner) {
                visible = false
                handoff.cancel() // A late host-stop callback must never open an app.
                voice.close()
            }
        })
    }

    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template {
        val message = when (handoff.phase) {
            HandoffPhase.READY -> R.string.handoff_ready
            HandoffPhase.RELEASING -> R.string.handoff_releasing
            HandoffPhase.REQUESTED -> R.string.handoff_requested
            HandoffPhase.UNAVAILABLE -> R.string.handoff_unavailable
            HandoffPhase.BLOCKED -> R.string.handoff_blocked
            HandoffPhase.LOCKED -> R.string.handoff_locked
            HandoffPhase.RELEASE_FAILED -> R.string.handoff_release_failed
        }
        val builder = MessageTemplate.Builder(carContext.getString(message))
            .setTitle(carContext.getString(R.string.car_title))
            .setHeaderAction(Action.BACK)
        val enabled = handoff.phase != HandoffPhase.RELEASING && handoff.phase != HandoffPhase.REQUESTED
        OfficialAssistant.entries.forEach { assistant ->
            builder.addAction(Action.Builder().setTitle(carContext.getString(assistant.label))
                .setBackgroundColor(CarColor.PRIMARY).setEnabled(enabled)
                .setOnClickListener(ParkedOnlyOnClickListener.create {
                    if (visible) handoff.request { launcher.open(assistant) }
                }).build())
        }
        return builder.build()
    }
}
