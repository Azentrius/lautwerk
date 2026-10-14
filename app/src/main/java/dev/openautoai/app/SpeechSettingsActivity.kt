// SPDX-License-Identifier: Apache-2.0
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package dev.openautoai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import dev.openautoai.core.i18n.AppLanguage
import dev.openautoai.app.design.*
import dev.openautoai.app.voice.SpeechPreview
import dev.openautoai.app.voice.SpeechSettingsStore
import dev.openautoai.speech.PocketAlba
import dev.openautoai.speech.R as SpeechR

/** Previews retain their existing lifetime and never run outside this foreground screen. */
class SpeechSettingsActivity : ComponentActivity() {
    private lateinit var preview: SpeechPreview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preview=SpeechPreview(this,SpeechSettingsStore(this))
        setContent { AzentriusTheme { Surface(Modifier.fillMaxSize()) { SpeechSettingsContent(preview,AppState.active,::finish) } } }
    }
    override fun onStart() { super.onStart();if(!AppState.active)preview.open() }
    override fun onStop() { preview.close();super.onStop() }
}

internal data class VoiceOption(val id:String?,val title:String,val description:String?=null,val online:Boolean=false,val enabled:Boolean=true)

/** Presentation-only confirmation: choosing still persists through the original callback. */
@Composable internal fun VoiceOptions(options:List<VoiceOption>,selectedId:String?,enabled:Boolean,onChoose:(String?)->Unit) {
    var pending by remember { mutableStateOf<VoiceOption?>(null) }
    Column(Modifier.fillMaxWidth().testTag("speech-voice-picker").selectableGroup()
        .clip(RoundedCornerShape(16.dp)).background(Az.colors["bg-card"],RoundedCornerShape(16.dp)).border(1.dp,Az.colors["line"],RoundedCornerShape(16.dp))) {
        options.forEachIndexed { index,option ->
            AzVoiceRow(option.title,selectedId==option.id,{
                if(option.online)pending=option else onChoose(option.id)
            },enabled && option.enabled,Modifier.testTag("voice-option-${option.id ?: "default"}"),description=option.description)
            if(index<options.lastIndex) HorizontalDivider(Modifier.padding(start=16.dp),color=Az.colors["line"],thickness=1.dp)
        }
    }
    pending?.let { option ->
        AzDialog(onDismissRequest={pending=null},title={Text(stringResource(R.string.online_voice_title))},
            text={Text(stringResource(R.string.online_voice_warning))},
            confirmButton={ AzPrimaryButton(onClick={if(enabled)onChoose(option.id);pending=null},enabled=enabled) { Text(stringResource(R.string.online_voice_choose)) } },
            dismissButton={ AzTextButton(onClick={pending=null}) { Text(stringResource(R.string.cancel)) } })
    }
}

@Composable private fun SpeechSettingsContent(preview:SpeechPreview,conversationActive:Boolean,back:()->Unit) {
    LaunchedEffect(conversationActive) { if(conversationActive)preview.close() }
    val resources=LocalResources.current
    val options=listOf(VoiceOption(null,stringResource(R.string.android_default),enabled=preview.androidReady))+preview.voices.mapIndexed { index,voice ->
        VoiceOption(voice.name,resources.getString(R.string.az_voice_name,index+1,voice.locale.getDisplayCountry(AppLanguage.locale())),
            resources.getString(if(voice.isNetworkConnectionRequired)R.string.online else R.string.on_device),voice.isNetworkConnectionRequired)
    }
    val allOptions = options + VoiceOption(PocketAlba.VOICE, stringResource(SpeechR.string.pocket_alba),
        preview.pocketNotice ?: stringResource(SpeechR.string.pocket_local), enabled=preview.pocketReady)
    val isPocket = PocketAlba.selected(preview.settings)
    LazyColumn(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).testTag("speech-list"),
        contentPadding=PaddingValues(Az.inset),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.voice_settings),style=AzType.title) }
        item { Text(stringResource(R.string.voice_hint)) }
        if(conversationActive)item { Text(stringResource(R.string.stop_first)) }
        item { Text(stringResource(R.string.online_voice_hint),style=AzType.label,color=Az.colors["text-subtle"]) }
        item {
            Text(stringResource(R.string.az_voice_section),style=AzType.section,modifier=Modifier.padding(top=8.dp,bottom=8.dp))
            VoiceOptions(allOptions,preview.settings.voice,preview.ready&&!conversationActive) { id ->
                if(id==PocketAlba.VOICE)preview.choosePocket()
                else if(id==null)preview.choose(null) else preview.voices.find { it.name==id }?.let(preview::choose)
            }
        }
        if(!isPocket && preview.settings.voice!=null && preview.voices.none { it.name==preview.settings.voice }) item { Text(stringResource(R.string.voice_missing)) }
        item { Text(stringResource(SpeechR.string.pocket_help),style=AzType.label,color=Az.colors["text-subtle"]) }
        if(preview.androidReady&&preview.voices.isEmpty())item { Text(stringResource(R.string.voices_empty)) }
        if(!preview.ready&&preview.notice==null&&!conversationActive)item { Text(stringResource(R.string.voices_loading)) }
        item {
            Text(stringResource(R.string.az_tempo_section),style=AzType.section,modifier=Modifier.padding(top=8.dp,bottom=8.dp))
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf(.9f to R.string.rate_slow,1f to R.string.rate_normal,1.1f to R.string.rate_fast).forEach { (rate,title) ->
                    AzChip((if(isPocket)1f else preview.settings.rate)==rate,{preview.rate(rate)}, { Text(stringResource(title)) },enabled=preview.ready&&!conversationActive&&!isPocket)
                }
            }
        }
        if(isPocket)item { Text(stringResource(SpeechR.string.pocket_rate),style=AzType.label,color=Az.colors["text-subtle"]) }
        item {
            AzActions(Modifier.fillMaxWidth()) {
                AzSecondaryButton(preview::play,enabled=preview.ready&&!preview.playing&&!conversationActive) {
                    AzIcon(R.drawable.ic_az_play);Text(stringResource(R.string.preview_play))
                }
                AzSecondaryButton(preview::stop,enabled=preview.playing) {
                    AzIcon(R.drawable.ic_az_stop);Text(stringResource(R.string.preview_stop))
                }
            }
        }
        item { Text(stringResource(R.string.preview_hint),style=AzType.label,color=Az.colors["text-subtle"]) }
        preview.notice?.let { item { Text(it) } }
        item { AzIconButton(R.drawable.ic_az_arrow_left,stringResource(R.string.back_conversation),back) }
    }
}
