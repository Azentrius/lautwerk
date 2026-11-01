// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.controls

import android.os.Handler
import android.os.Looper
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.azentrius.lautwerk.car.R
import com.azentrius.lautwerk.car.handoff.OfficialAssistant
import com.azentrius.lautwerk.car.voice.CarVoicePort
import com.azentrius.lautwerk.car.voice.VoiceConnection
import com.azentrius.lautwerk.car.voice.VoiceTimer
import com.azentrius.lautwerk.voice.AudioHandoff
import com.azentrius.lautwerk.voice.HandoffPhase
import com.azentrius.lautwerk.voice.ControlLeaseStore
import java.io.Closeable

/** Host-drawn diagnostic pages only. Existing API template and behavior remain unchanged. */
class NativeAssistantPicker(context: CarContext) : Screen(context) {
    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template = MessageTemplate.Builder(carContext.getString(R.string.native_picker))
        .setTitle(carContext.getString(R.string.car_title)).setHeaderAction(Action.BACK).apply {
            OfficialAssistant.entries.forEach { target ->
                addAction(Action.Builder().setTitle(target.displayName()).setBackgroundColor(CarColor.PRIMARY)
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED))
                            screenManager.push(NativeControlScreen(carContext, target))
                    }).build())
            }
        }.build()
}

fun OfficialAssistant.displayName(): String = when (this) { OfficialAssistant.CHATGPT -> "ChatGPT"; OfficialAssistant.CLAUDE -> "Claude" }

class NativeControlScreen(
    context: CarContext,
    private val target: OfficialAssistant,
    private val voice: CarVoicePort = VoiceConnection(context, context),
    private val controls: NativeControlPort = NativeControls.port(context)
) : Screen(context) {
    private var visible = false
    private var subscription: Closeable? = null
    private var result = NativeResult.READY
    private var lastEnd: ControlLeaseStore.Ticket? = null
    private val handoff = AudioHandoff(voice::releaseForHandoff, VoiceTimer(Handler(Looper.getMainLooper()))) {
        if (it == HandoffPhase.RELEASE_FAILED) result = NativeResult.RELEASE_FAILED
        if (visible) invalidate()
    }

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                visible = true
                voice.connect { if (visible) invalidate() }
                subscription = controls.observe { if (visible) invalidate() }
            }
            override fun onStop(owner: LifecycleOwner) {
                visible = false
                handoff.cancel()
                subscription?.close(); subscription = null
                voice.close()
            }
        })
    }

    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template {
        val ready = controls.readiness(target)
        val ticket = controls.ticket(target)
        if (handoff.phase == HandoffPhase.REQUESTED && ready == NativeResult.READY) handoff.cancel()
        val releasing = handoff.phase == HandoffPhase.RELEASING
        val status = when {
            releasing -> R.string.handoff_releasing
            result == NativeResult.END_REQUESTED -> R.string.native_end_requested
            ticket != null -> R.string.native_action_available
            result == NativeResult.RELEASE_FAILED -> R.string.handoff_release_failed
            result in listOf(NativeResult.BLOCKED, NativeResult.UNAVAILABLE) -> result.message()
            ready != NativeResult.READY && ready != NativeResult.BUSY -> ready.message()
            result == NativeResult.START_REQUESTED -> R.string.native_start_requested
            else -> ready.message()
        }
        val canStart = !releasing && ready == NativeResult.READY
        return MessageTemplate.Builder("${target.displayName()}\n${carContext.getString(status)}")
            .setTitle(carContext.getString(R.string.car_title)).setHeaderAction(Action.BACK)
            .addAction(Action.Builder().setTitle(carContext.getString(R.string.native_start, target.displayName()))
                .setBackgroundColor(CarColor.PRIMARY).setEnabled(canStart)
                .setOnClickListener(ParkedOnlyOnClickListener.create {
                    if (visible && canStart && controls.readiness(target) == NativeResult.READY) {
                        result = NativeResult.READY
                        handoff.request {
                            result = controls.start(target)
                            if (result == NativeResult.START_REQUESTED) HandoffPhase.REQUESTED else HandoffPhase.BLOCKED
                        }
                    }
                }).build())
            .addAction(Action.Builder().setTitle(carContext.getString(R.string.native_end))
                .setBackgroundColor(CarColor.PRIMARY).setEnabled(ticket != null && !releasing)
                .setOnClickListener(ParkedOnlyOnClickListener.create {
                    if (visible && !releasing && ticket != null && ticket != lastEnd) {
                        lastEnd = ticket
                        result = controls.end(ticket)
                        invalidate()
                    }
                }).build()).build()
    }
}

fun NativeResult.message(): Int = when (this) {
    NativeResult.READY -> R.string.native_ready
    NativeResult.NEEDS_ROLE -> R.string.native_needs_role
    NativeResult.NEEDS_LISTENER -> R.string.native_needs_listener
    NativeResult.NEEDS_SHORTCUT -> R.string.native_needs_shortcut
    NativeResult.LOCKED -> R.string.handoff_locked
    NativeResult.BUSY -> R.string.native_busy
    NativeResult.START_REQUESTED -> R.string.native_start_requested
    NativeResult.END_REQUESTED -> R.string.native_end_requested
    NativeResult.UNAVAILABLE -> R.string.native_action_unavailable
    NativeResult.BLOCKED -> R.string.native_blocked
    NativeResult.RELEASE_FAILED -> R.string.handoff_release_failed
}
