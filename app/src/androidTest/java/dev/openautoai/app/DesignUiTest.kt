// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.openautoai.app.design.*
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import dev.openautoai.voice.VoicePhase
import org.junit.*
import org.junit.Assert.*
import java.time.*
import java.util.Locale

/** Pure presentation fixtures: no TTS engine, microphone, real preferences, archive or HTTP client. */
class DesignUiTest {
    @get:Rule val compose=createComposeRule()
    @After fun reset() { compose.runOnIdle { AppState.update(false,Provider.OPENAI,ModelCatalog.default(Provider.OPENAI),null,"Test");AppState.reportIssue(null) } }
    private fun empty()=HomeState(ModelSelection(Provider.OPENAI,ModelCatalog.default(Provider.OPENAI)),loading=false)

    @Test fun tabsChangeTogetherWithMeasuredWidthAndKeepSelectedTabFullyVisible() {
        var width by mutableStateOf(600.dp)
        var scale by mutableStateOf(1f)
        var page by mutableStateOf("conversation")
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,scale)) {
                AzentriusTheme { Box(Modifier.requiredWidth(width)) { AzTabs(page){page=it} } }
            }
        }
        compose.onNodeWithTag("tabs-mode-1").assertExists()
        compose.runOnIdle { width=360.dp }
        compose.onNodeWithTag("tabs-mode-2").assertExists()
        fun labelsAreWhole() {
            listOf("Gespräch","Einstellungen","Verlauf").forEach { label ->
                val results=mutableListOf<TextLayoutResult>()
                compose.onNodeWithText(label,useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                assertEquals(1,results.single().lineCount)
                val layout=results.single()
                // Compose rounds the intrinsic float width to integer pixels; compare against available width.
                assertEquals(label.length,layout.getLineEnd(0,visibleEnd=true))
                assertTrue("$label must fit its available width",layout.getLineRight(0)<=layout.layoutInput.constraints.maxWidth)
                assertFalse(layout.didOverflowHeight)
            }
        }
        labelsAreWhole()
        compose.runOnIdle { scale=2f;page="history" }
        compose.onNodeWithTag("tabs-mode-3").assertExists()
        labelsAreWhole()
        fun selectedIsVisible(id:String) {
            compose.waitForIdle()
            val viewport=compose.onNodeWithTag("tabs-mode-3").getUnclippedBoundsInRoot()
            val tab=compose.onNodeWithTag("page-$id").getUnclippedBoundsInRoot()
            assertTrue(tab.left>=viewport.left && tab.right<=viewport.right)
            assertTrue(tab.width>0.dp)
        }
        selectedIsVisible("history")
        compose.runOnIdle { page="conversation" }
        selectedIsVisible("conversation")
    }

    @Test fun voiceRowsReserveSpaceForDescriptionAndGrowWithLargeText() {
        var scale by mutableStateOf(1f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                AzentriusTheme {
                    Column(Modifier.width(280.dp)) {
                        AzVoiceRow("System default", true, {}, true, Modifier.testTag("one-line"))
                        AzVoiceRow("Example voice", false, {}, true, Modifier.testTag("two-lines"),
                            "Online · Example engine")
                    }
                }
            }
        }
        compose.onNodeWithTag("one-line").assertHeightIsAtLeast(56.dp)
        compose.onNodeWithTag("two-lines").assertHeightIsAtLeast(72.dp)
        compose.runOnIdle { scale = 2f }
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("Online · Example engine", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertFalse(results.single().didOverflowHeight)
        compose.onNodeWithTag("two-lines").assertHeightIsAtLeast(72.dp)
    }

    @Test fun bundledLicensesRemainScrollableAndCanBeClosedInDarkTheme() {
        var open by mutableStateOf(true)
        compose.setContent { AzentriusTheme(dark=true) { if(open) LicenseDialog { open=false } } }
        compose.onNodeWithText("Lizenzen").assertIsDisplayed()
        compose.onNodeWithText("Schließen").performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText("Lizenzen").assertDoesNotExist()
    }

    @Test fun activePreparingAlwaysStopsAndPendingInactiveStartOnlyCancels() {
        var state by mutableStateOf(empty());var stopped=0;var cancelled=0;var started=0
        compose.setContent { AzentriusTheme { ConversationBar(state,{started++},{stopped++},{cancelled++},{},{}) } }
        compose.runOnIdle { AppState.update(true,Provider.OPENAI,"test",null,"Test",VoicePhase.PREPARING) }
        compose.onNodeWithTag("conversation-stop").assertIsEnabled().performClick()
        compose.onNodeWithTag("start-cancel").assertDoesNotExist()
        compose.runOnIdle { AppState.update(false,Provider.OPENAI,"test",null,"Test");state=empty().copy(operation=HomeOperation.START) }
        compose.onNodeWithTag("start-cancel").performClick()
        assertEquals(1,stopped);assertEquals(1,cancelled);assertEquals(0,started)
    }
    @Test fun resumeAndRetiredModelExportKeepIndependentAvailability() {
        val chat=Conversation("fake","Fake",Provider.OPENAI,"retired",0,emptyList())
        compose.setContent { AzentriusTheme { ConversationBar(empty().copy(sessions=listOf(chat),selectedId=chat.id),{},{},{},{},{}) } }
        compose.onNodeWithTag("conversation-start").assertIsNotEnabled()
        compose.onNodeWithText("Gespräch fortsetzen").assertExists()
        compose.onNodeWithTag("export-chat").assertIsEnabled()
    }
    @Test fun exportPreparationPickerWritingAndCompletionExposeOnlyExistingActions() {
        var state by mutableStateOf(empty());var cancelled=0
        compose.setContent { AzentriusTheme { ConversationBar(state,{},{},{},{cancelled++},{}) } }
        compose.runOnIdle { state=empty().copy(operation=HomeOperation.PREPARE_EXPORT,exportPresentation=ExportPresentation.PREPARING) }
        compose.onNodeWithTag("export-cancel").performClick()
        compose.onNodeWithTag("conversation-status").assertExists()
        compose.onNodeWithTag("export-status").assertExists()
        compose.runOnIdle { state=state.copy(operation=HomeOperation.PICK_EXPORT) }
        compose.onNodeWithTag("export-cancel").assertDoesNotExist();compose.onNodeWithTag("export-chat").assertDoesNotExist()
        compose.runOnIdle { state=state.copy(operation=HomeOperation.WRITE_EXPORT,exportPresentation=ExportPresentation.WRITING) }
        compose.onNodeWithTag("export-cancel").assertDoesNotExist();compose.onNodeWithTag("export-chat").assertIsNotEnabled()
        assertEquals(1,cancelled)
    }
    @Test fun onlineSelectionAlwaysConfirmsAndCancelOrOfflineChoiceNeverImplicitlyAcceptsIt() {
        var selected by mutableStateOf<String?>("local");val selections=mutableListOf<String?>()
        val options=listOf(VoiceOption(null,"Android-Standard"),VoiceOption("local","Teststimme lokal","Auf dem Gerät"),VoiceOption("cloud","Teststimme online","Online",true))
        compose.setContent { AzentriusTheme { VoiceOptions(options,selected,true) { selected=it;selections+=it } } }
        compose.onNodeWithTag("voice-option-cloud").performClick()
        compose.onNodeWithTag("voice-option-local").assertIsSelected()
        assertTrue(selections.isEmpty())
        compose.onNodeWithText("Abbrechen").performClick();assertTrue(selections.isEmpty())
        compose.onNodeWithTag("voice-option-cloud").performClick()
        compose.onNodeWithText("Online-Stimme wählen").performClick();assertEquals(listOf("cloud"),selections)
        compose.onNodeWithTag("voice-option-cloud").performClick()
        compose.onNodeWithText("Online-Stimme wählen").assertExists()
        compose.onNodeWithText("Abbrechen").performClick();assertEquals(1,selections.size)
        compose.onNodeWithTag("voice-option-default").performClick();assertNull(selected)
        compose.onNodeWithText("Online-Stimme wählen").assertDoesNotExist()
        compose.onNodeWithTag("voice-option-local").performClick();assertEquals("local",selected)
    }
    @Test fun disabledCatalogCannotChooseOrOpenAConfirmation() {
        var count=0
        compose.setContent { AzentriusTheme { VoiceOptions(listOf(VoiceOption("cloud","Teststimme","Online",true)),null,false){count++} } }
        compose.onNodeWithTag("voice-option-cloud").assertIsNotEnabled().performClick()
        compose.onNodeWithText("Online-Stimme wählen").assertDoesNotExist();assertEquals(0,count)
    }
    @Test fun messageIsOneAccessibleElementWithRoleAndFullContent() {
        compose.setContent { AzentriusTheme { AzMessage("assistant","Only invented content","Assistent: Only invented content",Modifier.testTag("message")) } }
        compose.onNodeWithTag("message").assertContentDescriptionEquals("Assistent: Only invented content")
    }
    @Test fun historyUsesLastModifiedTimeAndDeviceLocaleAcrossCalendarBoundaries() {
        val zone=ZoneId.of("Europe/Berlin");val today=LocalDate.of(2026,10,12)
        fun at(day:LocalDate)=day.atTime(8,14).atZone(zone).toInstant().toEpochMilli()
        assertEquals("Heute, 08:14",historyDate(at(today),Locale.GERMANY,zone,today))
        assertEquals("Gestern, 08:14",historyDate(at(today.minusDays(1)),Locale.GERMANY,zone,today))
        assertTrue(historyDate(at(today.minusYears(1)),Locale.GERMANY,zone,today).contains("2025"))
        assertTrue(historyDate(at(today),Locale.US,zone,today).startsWith("Today, "))
        assertTrue(historyDate(at(today.minusDays(1)),Locale.US,zone,today).startsWith("Yesterday, "))
    }
}
