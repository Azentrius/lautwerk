// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.os.Bundle
import android.speech.SpeechRecognizer
import com.azentrius.lautwerk.car.voice.FinalSpeechListener
import org.junit.Assert.assertEquals
import org.junit.Test

class FinalSpeechListenerTest {
    private fun result(text: String) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(text))
    }

    @Test fun emptyCarSegmentsAndSpeechBoundariesKeepWaitingForTheQuestion() {
        val texts = mutableListOf<String>()
        val errors = mutableListOf<Int>()
        val listener = FinalSpeechListener(texts::add, errors::add, segmented = true)
        listener.onResults(Bundle())
        listener.onSegmentResults(result("  "))
        listener.onEndOfSpeech()
        assertEquals(emptyList<String>(), texts)
        assertEquals(emptyList<Int>(), errors)
        listener.onSegmentResults(result("Later question"))
        listener.onEndOfSegmentedSession()
        listener.onResults(result("Duplicate"))
        assertEquals(listOf("Later question"), texts)
        assertEquals(emptyList<Int>(), errors)
    }

    @Test fun emptyPhoneResultIsStillTerminal() {
        val texts = mutableListOf<String>()
        val listener = FinalSpeechListener(texts::add, { throw AssertionError("Unexpected error") })
        listener.onResults(Bundle())
        listener.onResults(result("Too late"))
        assertEquals(listOf(""), texts)
    }

    @Test fun segmentedResultIsDeliveredOnceDespiteLaterResultsAndErrors() {
        val texts = mutableListOf<String>()
        val errors = mutableListOf<Int>()
        val listener = FinalSpeechListener(texts::add, errors::add, segmented = true)
        listener.onSegmentResults(result("Synthetic question"))
        listener.onResults(Bundle())
        listener.onSegmentResults(result("Duplicate"))
        listener.onEndOfSegmentedSession()
        listener.onError(SpeechRecognizer.ERROR_CLIENT)
        assertEquals(listOf("Synthetic question"), texts)
        assertEquals(emptyList<Int>(), errors)
    }

    @Test fun partialResultsAreNeverSubmittedAndEmptySessionFailsOnce() {
        val texts = mutableListOf<String>()
        val errors = mutableListOf<Int>()
        val listener = FinalSpeechListener(texts::add, errors::add, segmented = true)
        listener.onResults(Bundle())
        listener.onPartialResults(result("Unfinished question"))
        listener.onEndOfSegmentedSession()
        listener.onResults(result("Too late"))
        listener.onError(SpeechRecognizer.ERROR_CLIENT)
        assertEquals(emptyList<String>(), texts)
        assertEquals(listOf(SpeechRecognizer.ERROR_NO_MATCH), errors)
    }

    @Test fun phoneResultsAndEndOfSpeechUseTheSameFinalCallback() {
        val texts = mutableListOf<String>()
        var ends = 0
        val listener = FinalSpeechListener(texts::add, { throw AssertionError("Unexpected error") }, { ends++ })
        listener.onEndOfSpeech()
        listener.onResults(result("Phone question"))
        assertEquals(1, ends)
        assertEquals(listOf("Phone question"), texts)
    }
}
