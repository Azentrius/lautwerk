@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package dev.openautoai.app.design

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.*
import dev.openautoai.app.R
import dev.openautoai.core.i18n.AppLanguage
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** All tabs use one mode, chosen from actual text metrics at the current system font scale. */
@Composable internal fun AzTabs(page:String,onSelect:(String)->Unit) {
    val c=Az.colors
    val entries=listOf(Triple("conversation",R.string.conversation,R.drawable.ic_az_comments),
        Triple("settings",R.string.settings,R.drawable.ic_az_cog),Triple("history",R.string.history,R.drawable.ic_az_history))
    val labels=entries.map { stringResource(it.second) }
    val measurer=rememberTextMeasurer()
    val density=LocalDensity.current
    val inlineWidths=labels.map { measurer.measure(AnnotatedString(it),AzType.control,maxLines=1,softWrap=false).size.width }
    val stackedWidths=labels.map { measurer.measure(AnnotatedString(it),AzType.label,maxLines=1,softWrap=false).size.width }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width=constraints.maxWidth
        val inlineRequired=inlineWidths.max()+with(density) { (24+8+32).dp.roundToPx() }
        val stackedRequired=maxOf(stackedWidths.max(),with(density){24.dp.roundToPx()})+with(density){16.dp.roundToPx()}
        val mode=when { inlineRequired*entries.size<=width->1;stackedRequired*entries.size<=width->2;else->3 }
        val scroll=rememberScrollState()
        val individual=stackedWidths.map { maxOf(it,with(density){24.dp.roundToPx()})+with(density){32.dp.roundToPx()} }
        LaunchedEffect(page,mode,width,individual) {
            if(mode==3) {
                val index=entries.indexOfFirst { it.first==page }.coerceAtLeast(0)
                val start=individual.take(index).sum()+with(density){16.dp.roundToPx()}
                val end=start+individual[index]
                scroll.scrollTo(when { start<scroll.value->start;end>scroll.value+width->end-width;else->scroll.value })
            }
        }
        Row(Modifier.fillMaxWidth().testTag("tabs-mode-$mode")
            .drawBehind { drawLine(c["line"],Offset(0f,size.height),Offset(size.width,size.height),1.dp.toPx()) }
            .then(if(mode==3)Modifier.horizontalScroll(scroll).padding(horizontal=16.dp) else Modifier)
            .height(IntrinsicSize.Min)) {
            entries.forEachIndexed { index,(id,_,icon) ->
                val interaction=remember { MutableInteractionSource() };val pressed by interaction.collectIsPressedAsState()
                val focused by interaction.collectIsFocusedAsState();val selected=id==page
                val marker=if(mode==1)inlineWidths[index]+with(density){32.dp.roundToPx()} else stackedWidths[index]
                val sizing=if(mode==3)Modifier.width(with(density){individual[index].toDp()}) else Modifier.weight(1f)
                Box(sizing.fillMaxHeight().heightIn(min=if(mode==1)48.dp else 64.dp).testTag("page-$id")
                    .focusRing(focused&&androidx.compose.ui.platform.LocalInputModeManager.current.inputMode==androidx.compose.ui.input.InputMode.Keyboard,RoundedCornerShape(0.dp),c["focus"])
                    .background(if(pressed)c["bg-hover"] else c["bg-page"])
                    .selectable(selected,interactionSource=interaction,indication=null,role=Role.Tab,onClick={onSelect(id)})
                    .drawBehind {
                        if(selected) drawLine(c["primary-text"],Offset((size.width-marker)/2+1.5.dp.toPx(),size.height),
                            Offset((size.width+marker)/2-1.5.dp.toPx(),size.height),3.dp.toPx(),StrokeCap.Round)
                    }.padding(horizontal=if(mode==2)8.dp else 16.dp,vertical=8.dp),contentAlignment=Alignment.Center) {
                    CompositionLocalProvider(LocalContentColor provides c[if(selected)"primary-text" else "text-subtle"]) {
                        if(mode==1) Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            AzIcon(icon,24.dp);Text(labels[index],style=AzType.control,maxLines=1,softWrap=false)
                        } else Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            AzIcon(icon,24.dp);Text(labels[index],style=AzType.label,maxLines=1,softWrap=false)
                        }
                    }
                }
            }
        }
    }
}

@Composable internal fun AzMessage(role:String,text:String,description:String,modifier:Modifier=Modifier) {
    val user=role=="user";val c=Az.colors
    Box(modifier.fillMaxWidth().clearAndSetSemantics { contentDescription=description }) {
        Text(text,style=AzType.body,color=c[if(user)"on-tint-neutral" else "text"],
            modifier=if(user) Modifier.align(Alignment.TopEnd).fillMaxWidth(.8f)
                .background(c["tint-neutral"],RoundedCornerShape(topStart=16.dp,topEnd=16.dp,bottomStart=16.dp,bottomEnd=6.dp))
                .padding(horizontal=16.dp,vertical=8.dp) else Modifier.fillMaxWidth())
    }
}

@Composable internal fun AzHistoryRow(title:String,subtitle:String,match:String?,onClick:()->Unit,enabled:Boolean,
    first:Boolean,last:Boolean,modifier:Modifier=Modifier) {
    val c=Az.colors;val interaction=remember { MutableInteractionSource() };val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val shape=RoundedCornerShape(topStart=if(first)16.dp else 0.dp,topEnd=if(first)16.dp else 0.dp,
        bottomStart=if(last)16.dp else 0.dp,bottomEnd=if(last)16.dp else 0.dp)
    Row(modifier.fillMaxWidth().heightIn(min=if(match!=null)88.dp else 72.dp)
        .focusRing(focused&&androidx.compose.ui.platform.LocalInputModeManager.current.inputMode==androidx.compose.ui.input.InputMode.Keyboard,shape,c["focus"])
        .background(c[if(!enabled)"tint-neutral" else if(pressed)"bg-hover" else "bg-card"],shape)
        .drawWithContent {
            drawContent()
            val stroke=1.dp.toPx()
            clipRect(top=if(first)-stroke else stroke,bottom=if(last)size.height+stroke else size.height-stroke) {
                drawOutline(shape.createOutline(size,layoutDirection,this),c["line"],style=Stroke(stroke))
            }
            if(!last) drawLine(c["line"],Offset(16.dp.toPx(),size.height-stroke/2),Offset(size.width,size.height-stroke/2),stroke)
        }.clip(shape).clickable(interaction,indication=null,enabled=enabled,role=Role.Button,onClick=onClick)
        .padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(title,style=AzType.body,color=c[if(enabled)"text" else "text-disabled"],maxLines=1,overflow=TextOverflow.Ellipsis)
            Text(subtitle,style=AzType.label,color=c[if(enabled)"text-subtle" else "text-disabled"],maxLines=1,overflow=TextOverflow.Ellipsis)
            match?.let { Text(it,style=AzType.label,color=c[if(enabled)"text-subtle" else "text-disabled"]) }
        }
        CompositionLocalProvider(LocalContentColor provides c[if(enabled)"text-subtle" else "text-disabled"]) { AzIcon(R.drawable.ic_az_angle_right,24.dp) }
    }
}

@Composable internal fun AzVoiceRow(title:String,selected:Boolean,onClick:()->Unit,enabled:Boolean,modifier:Modifier=Modifier,description:String?=null) {
    val c=Az.colors;val interaction=remember { MutableInteractionSource() };val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    Row(modifier.fillMaxWidth().heightIn(min=56.dp)
        .focusRing(focused&&androidx.compose.ui.platform.LocalInputModeManager.current.inputMode==androidx.compose.ui.input.InputMode.Keyboard,RoundedCornerShape(0.dp),c["focus"])
        .background(if(pressed&&enabled)c["bg-hover"] else if(!enabled)c["tint-neutral"] else Color.Transparent)
        .selectable(selected,interactionSource=interaction,indication=null,enabled=enabled,role=Role.RadioButton,onClick=onClick)
        .padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
        Canvas(Modifier.size(20.dp)) {
            val color=c[if(!enabled)"text-disabled" else if(selected)"primary" else "line-control"]
            drawCircle(color,radius=9.dp.toPx(),style=Stroke(2.dp.toPx()))
            if(selected) drawCircle(color,radius=5.dp.toPx())
        }
        Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(title,style=AzType.body,color=c[if(enabled)"text" else "text-disabled"])
            description?.let { Text(it,style=AzType.label,color=c[if(enabled)"text-subtle" else "text-disabled"]) }
        }
    }
}

/** Uses the stored last-modified instant, never invents a duration or creation timestamp. */
internal fun historyDate(epoch:Long,locale:Locale=AppLanguage.locale(),zone:ZoneId=ZoneId.systemDefault(),today:LocalDate=LocalDate.now(zone)):String {
    val date=Instant.ofEpochMilli(epoch).atZone(zone)
    val de=locale.language=="de"
    return when(date.toLocalDate()) {
        today -> (if(de)"Heute, " else "Today, ")+date.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        today.minusDays(1) -> (if(de)"Gestern, " else "Yesterday, ")+date.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        else -> date.format(DateTimeFormatter.ofPattern(if(date.year==today.year) { if(de)"EEE, d. MMM" else "EEE, MMM d" }
            else { if(de)"d. MMM yyyy" else "MMM d, yyyy" },locale))
    }
}
