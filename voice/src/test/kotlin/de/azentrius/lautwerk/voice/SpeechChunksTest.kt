// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.voice

import org.junit.Assert.*
import org.junit.Test

class SpeechChunksTest {
    @Test fun longTextRemainsWithinTtsLimitWithoutBreakingEmoji() {
        val text = "Eine längere Antwort mit 🚗 und Umlauten. ".repeat(300)
        val chunks = SpeechChunks.split(text, 101)
        assertEquals(text, chunks.joinToString(""))
        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= 101 && !it.last().isHighSurrogate() && !it.first().isLowSurrogate() })
    }
    @Test fun tinyLimitStillPreservesSurrogatePairsAndEmptyInputProducesNoSpeech() {
        assertEquals(listOf("🚗", "🚙"), SpeechChunks.split("🚗🚙", 2))
        assertTrue(SpeechChunks.split("   ", 2).isEmpty())
        assertTrue(SpeechChunks.split("", 10).isEmpty())
    }
}
