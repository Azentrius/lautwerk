// SPDX-License-Identifier: Apache-2.0
package de.azentrius.lautwerk.app.voice

import android.os.Parcel
import android.text.Spanned
import android.text.TextUtils
import android.text.style.TtsSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.azentrius.lautwerk.core.i18n.AppLanguage
import de.azentrius.lautwerk.core.i18n.Messages
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** The TTS request crosses a Binder boundary; verify that the pronunciation hint survives it. */
@RunWith(AndroidJUnit4::class)
class SpeechPreviewPronunciationTest {
    @Test fun germanAndEnglishSamplesKeepTheirTextAndSpellOnlyTheBrandSuffixAcrossParcel() {
        val previous = AppLanguage.preferences
        try {
            for (locale in listOf(Locale.GERMAN, Locale.ENGLISH)) {
                AppLanguage.preferences = { listOf(locale) }
                val sentence = Messages.PREVIEW_SENTENCE.text()
                val parcel = Parcel.obtain()
                try {
                    TextUtils.writeToParcel(previewSpeechText(sentence), parcel, 0)
                    parcel.setDataPosition(0)
                    val received = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel) as Spanned
                    assertEquals(sentence, received.toString())
                    val span = received.getSpans(0, received.length, TtsSpan::class.java).single()
                    assertEquals(TtsSpan.TYPE_VERBATIM, span.type)
                    assertEquals("AI", span.args.getString(TtsSpan.ARG_VERBATIM))
                    val start = received.getSpanStart(span)
                    assertEquals("OpenAuto ", received.subSequence(start - 9, start).toString())
                    assertEquals("AI", received.subSequence(start, received.getSpanEnd(span)).toString())
                } finally { parcel.recycle() }
            }
        } finally { AppLanguage.preferences = previous }
    }

    @Test fun aSampleWithoutTheBrandIsReturnedUnchanged() {
        val sentence = "This sample does not contain the application name."
        assertSame(sentence, previewSpeechText(sentence))
    }
}
