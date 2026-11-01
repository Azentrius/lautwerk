// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.controls

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/** System-bound role holder only. No hotword, audio capture or native session impersonation. */
class ControlVoiceInteractionService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
        setDisabledShowContext(VoiceInteractionSession.SHOW_WITH_ASSIST or VoiceInteractionSession.SHOW_WITH_SCREENSHOT)
    }
}

/** The system assistant gesture deliberately does not start another app or inspect context. */
class ControlVoiceSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession = object : VoiceInteractionSession(this) {
        override fun onShow(args: Bundle?, showFlags: Int) { finish() }
    }
}

/** Required voice-interaction metadata companion, not a replacement speech recognition engine. */
class ControlRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback) { listener.error(SpeechRecognizer.ERROR_CLIENT) }
    override fun onStopListening(listener: Callback) { listener.error(SpeechRecognizer.ERROR_CLIENT) }
    override fun onCancel(listener: Callback) = Unit
}
