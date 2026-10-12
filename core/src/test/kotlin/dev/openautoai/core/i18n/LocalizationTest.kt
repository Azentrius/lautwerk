package dev.openautoai.core.i18n

import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import java.io.StringWriter
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class LocalizationTest {
    @Test fun supportedPreferencesPreserveRegionAndOtherLanguagesFallBackToEnglish() {
        assertEquals(Locale.GERMANY, AppLanguage.resolve(listOf(Locale.GERMANY, Locale.US)))
        assertEquals(Locale.UK, AppLanguage.resolve(listOf(Locale.UK, Locale.GERMANY)))
        assertEquals(Locale.GERMAN, AppLanguage.resolve(listOf(Locale.FRENCH, Locale.GERMAN)))
        assertEquals(Locale.ENGLISH, AppLanguage.resolve(listOf(Locale.JAPANESE)))
        assertEquals(Locale.ENGLISH, AppLanguage.resolve(emptyList()))
    }
    @Test fun allFixedMessagesFormatInBothLanguagesWithoutLeakingPlaceholders() {
        for (message in Messages.entries) for (locale in listOf(Locale.GERMAN, Locale.ENGLISH)) {
            val rendered=message.text("Example",locale=locale)
            assertTrue(rendered.isNotBlank()); assertFalse(rendered.contains("%s"))
            assertFalse(rendered.contains("Sprachgespräch"))
        }
        assertEquals("Ready",Messages.READY.text(locale=Locale.ENGLISH))
        assertEquals("Bereit",Messages.READY.text(locale=Locale.GERMAN))
    }
    @Test fun changingLanguageChangesErrorMessagesAfterEnumsHaveAlreadyLoaded() {
        val old=AppLanguage.preferences
        try {
            AppLanguage.preferences={listOf(Locale.GERMAN)}
            val german=ChatError.MISSING_KEY.userMessage
            AppLanguage.preferences={listOf(Locale.ENGLISH)}
            assertNotEquals(german,ChatError.MISSING_KEY.userMessage)
            assertEquals("Please save an API key for this provider first.",ChatFailure(ChatError.MISSING_KEY).message)
            assertTrue(StorageFailure(StorageError.ARCHIVE_READ).message!!.startsWith("The local"))
        } finally { AppLanguage.preferences=old }
    }
    @Test fun exportLocalizesOnlyLabelsAndPreservesExistingContents() {
        val chat=Conversation("id","Mein Gespräch",Provider.CLAUDE,"retired",0,listOf(Turn("user","Grüße 100% 🦊")))
        val out=StringWriter();ConversationTextExport.write(chat,out,locale=Locale.ENGLISH)
        assertTrue(out.toString().contains("Messages: 1"));assertTrue(out.toString().contains("Message 1 · You"))
        assertTrue(out.toString().contains(chat.title));assertTrue(out.toString().contains(chat.turns.single().content))
    }
}
