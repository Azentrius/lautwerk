// SPDX-License-Identifier: Apache-2.0
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.azentrius.lautwerk.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azentrius.lautwerk.app.design.*
import com.azentrius.lautwerk.core.ai.ModelCatalog
import com.azentrius.lautwerk.voice.VoicePhase

internal enum class ExportPresentation { NONE, PREPARING, WRITING, DONE, FAILED, CANCELLED }

@Composable internal fun ConversationBar(state:HomeState,onStart:()->Unit,onStop:()->Unit,onCancelStart:()->Unit,
    onCancelExport:()->Unit,onExport:()->Unit) {
    val active=AppState.active
    val pending=state.operation==HomeOperation.START||state.readyStart!=null
    val editable=!active&&state.operation==null&&state.readyStart==null
    val phase=if(!active&&pending)VoicePhase.PREPARING else AppState.phase
    val issue=AppState.notice?.takeIf { !active }
    val icon=when {
        issue!=null->R.drawable.ic_az_exclamation_circle
        phase==VoicePhase.PREPARING->R.drawable.ic_az_pending
        phase==VoicePhase.LISTENING->R.drawable.ic_az_microphone
        phase==VoicePhase.THINKING->R.drawable.ic_az_comment_dots
        phase==VoicePhase.SPEAKING->R.drawable.ic_az_volume_up
        phase==VoicePhase.STOPPED->R.drawable.ic_az_stop_circle
        else->R.drawable.ic_az_microphone_slash
    }
    val status=when {
        issue!=null->AzStatus.DANGER
        phase==VoicePhase.LISTENING->AzStatus.LISTENING
        phase==VoicePhase.THINKING||phase==VoicePhase.SPEAKING->AzStatus.INFO
        else->AzStatus.NEUTRAL
    }
    val label=issue?:stringResource(when(phase) {
        VoicePhase.PREPARING->R.string.az_preparing
        VoicePhase.LISTENING->R.string.az_listening
        VoicePhase.THINKING->R.string.az_thinking
        VoicePhase.SPEAKING->R.string.az_speaking
        VoicePhase.STOPPED->R.string.az_ended
        null->R.string.az_ready
    })
    Column(Modifier.fillMaxWidth().background(Az.colors["bg-page"])) {
        HorizontalDivider(color=Az.colors["line"],thickness=1.dp)
        Box(Modifier.fillMaxWidth().navigationBarsPadding().background(Az.colors["bg-card"]).imePadding().padding(16.dp)) {
            AzActions(Modifier.fillMaxWidth(),spaceBetween=true) {
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    AzStatusLabel(label,icon,status,Modifier.testTag("conversation-status"))
                    if(state.exportPresentation!=ExportPresentation.NONE) {
                        val export=state.exportPresentation
                        val text=stringResource(when(export) {
                            ExportPresentation.PREPARING->R.string.az_export_preparing
                            ExportPresentation.WRITING->R.string.az_export_writing
                            ExportPresentation.DONE->R.string.az_export_done
                            ExportPresentation.FAILED->R.string.az_export_failed
                            else->R.string.az_export_cancelled
                        })
                        AzStatusLabel(text,when(export) {
                            ExportPresentation.DONE->R.drawable.ic_az_check_circle
                            ExportPresentation.FAILED->R.drawable.ic_az_exclamation_circle
                            ExportPresentation.CANCELLED->R.drawable.ic_az_ban
                            else->R.drawable.ic_az_pending
                        },when(export) {
                            ExportPresentation.DONE->AzStatus.SUCCESS
                            ExportPresentation.FAILED->AzStatus.DANGER
                            else->AzStatus.NEUTRAL
                        },Modifier.testTag("export-status"))
                    }
                }
                AzActions {
                    if(state.operation==HomeOperation.PREPARE_EXPORT) {
                        AzTextButton(onCancelExport,modifier=Modifier.testTag("export-cancel")) { Text(stringResource(R.string.export_cancel)) }
                    } else if(state.operation!=HomeOperation.PICK_EXPORT) {
                        AzIconButton(R.drawable.ic_az_file_export,stringResource(R.string.export_chat),onExport,
                            enabled=editable&&!state.loading&&state.archiveError==null&&state.selected!=null,
                            modifier=Modifier.testTag("export-chat"))
                    }
                    when {
                        active->AzPrimaryButton(onStop,modifier=Modifier.testTag("conversation-stop")) {
                            AzIcon(R.drawable.ic_az_stop);Text(stringResource(R.string.conversation_stop))
                        }
                        pending->AzTextButton(onCancelStart,modifier=Modifier.testTag("start-cancel")) { Text(stringResource(R.string.start_cancel)) }
                        else->AzPrimaryButton(onStart,enabled=editable&&!state.loading&&state.archiveError==null&&state.selected?.let { ModelCatalog.valid(it.provider,it.modelId) }!=false,
                            modifier=Modifier.testTag("conversation-start")) {
                            AzIcon(R.drawable.ic_az_microphone)
                            Text(stringResource(if(state.selectedId!=null)R.string.conversation_resume else R.string.conversation_start))
                        }
                    }
                }
            }
        }
    }
}
