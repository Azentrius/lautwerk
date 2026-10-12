@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package dev.openautoai.app.design

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.openautoai.app.R

private val pill = RoundedCornerShape(50)
internal enum class AzButtonKind { PRIMARY, SECONDARY, TEXT, DANGER }

internal fun Modifier.focusRing(focused: Boolean, shape: Shape, color: Color): Modifier = drawBehind {
    if(focused) {
        val offset=3.dp.toPx()
        val outline=shape.createOutline(Size(size.width+2*offset,size.height+2*offset),layoutDirection,this)
        translate(-offset,-offset) { drawOutline(outline,color,style=Stroke(2.dp.toPx())) }
    }
}

@Composable
internal fun AzButton(onClick: () -> Unit, modifier: Modifier=Modifier, enabled: Boolean=true,
                      kind: AzButtonKind=AzButtonKind.PRIMARY, content: @Composable RowScope.() -> Unit) {
    val c=Az.colors
    val interactions=remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val focused by interactions.collectIsFocusedAsState()
    val keyboard=LocalInputModeManager.current.inputMode==InputMode.Keyboard
    val bg=when {
        !enabled -> if(kind==AzButtonKind.TEXT) Color.Transparent else c["tint-neutral"]
        kind==AzButtonKind.PRIMARY -> c[if(pressed) "primary-hover" else "primary"]
        kind==AzButtonKind.DANGER -> if(pressed) Color(0x14000000).compositeOver(c["status-danger"]) else c["status-danger"]
        kind==AzButtonKind.SECONDARY && pressed -> c["bg-hover"]
        else -> Color.Transparent
    }
    val fg=when {
        !enabled -> c["text-disabled"]
        kind==AzButtonKind.PRIMARY -> c["on-primary"]
        kind==AzButtonKind.DANGER -> c["on-danger"]
        kind==AzButtonKind.TEXT -> c[if(pressed) "link-hover" else "link"]
        else -> c["primary-text"]
    }
    val border=if(kind==AzButtonKind.SECONDARY && enabled)
        Modifier.border(if(pressed) 2.dp else 1.dp,c[if(pressed) "primary-hover" else "primary"],pill) else Modifier
    Row(modifier.defaultMinSize(minWidth=48.dp,minHeight=48.dp)
        .focusRing(focused&&keyboard,pill,c["focus"]).background(bg,pill).then(border)
        .clip(pill).clickable(interactions,indication=null,enabled=enabled,role=Role.Button,onClick=onClick)
        .padding(horizontal=if(kind==AzButtonKind.TEXT) 8.dp else 24.dp,vertical=8.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp,Alignment.CenterHorizontally)) {
        CompositionLocalProvider(LocalContentColor provides fg) { ProvideTextStyle(AzType.control) { content() } }
    }
}
@Composable internal fun AzPrimaryButton(onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,content:@Composable RowScope.()->Unit) =
    AzButton(onClick,modifier,enabled,AzButtonKind.PRIMARY,content)
@Composable internal fun AzSecondaryButton(onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,content:@Composable RowScope.()->Unit) =
    AzButton(onClick,modifier,enabled,AzButtonKind.SECONDARY,content)
@Composable internal fun AzTextButton(onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,content:@Composable RowScope.()->Unit) =
    AzButton(onClick,modifier,enabled,AzButtonKind.TEXT,content)

@Composable internal fun AzIcon(id:Int, size:Dp=20.dp, description:String?=null) {
    Icon(painterResource(id),description,Modifier.size(size),tint=LocalContentColor.current)
}
@Composable internal fun AzIconButton(id:Int, description:String,onClick:()->Unit,enabled:Boolean=true,modifier:Modifier=Modifier) {
    val c=Az.colors;val interaction=remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState();val focused by interaction.collectIsFocusedAsState()
    Box(modifier.size(48.dp).focusRing(focused&&LocalInputModeManager.current.inputMode==InputMode.Keyboard,pill,c["focus"])
        .background(if(pressed&&enabled)c["bg-hover"] else Color.Transparent,pill).clip(pill)
        .clickable(interaction,indication=null,enabled=enabled,role=Role.Button,onClick=onClick),contentAlignment=Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides c[if(!enabled)"text-disabled" else if(pressed)"text" else "text-subtle"]) {
            AzIcon(id,24.dp,description)
        }
    }
}

/** Chooses a row only when the actual labels fit; otherwise each action gets a full-width row. */
@Composable internal fun AzActions(modifier:Modifier=Modifier, gap:Dp=8.dp,spaceBetween:Boolean=false,content:@Composable ()->Unit) {
    Layout(content=content,modifier=modifier) { children,constraints ->
        val space=gap.roundToPx()
        val fits=children.sumOf { it.maxIntrinsicWidth(Constraints.Infinity).toLong() } + space.toLong()*(children.size-1)<=constraints.maxWidth
        val placed=children.map { it.measure(Constraints(minWidth=if(fits)0 else constraints.maxWidth,maxWidth=constraints.maxWidth)) }
        val width=if(fits) placed.sumOf { it.width }+space*(placed.size-1).coerceAtLeast(0) else constraints.maxWidth
        val height=if(fits) placed.maxOfOrNull { it.height }?:0 else placed.sumOf { it.height }+space*(placed.size-1).coerceAtLeast(0)
        layout(constraints.constrainWidth(width),constraints.constrainHeight(height)) {
            var offset=0
            val extra=if(fits&&spaceBetween&&placed.size>1)(constraints.constrainWidth(width)-width)/(placed.size-1) else 0
            placed.forEach { if(fits) { it.placeRelative(offset,(height-it.height)/2);offset+=it.width+space+extra }
                else { it.placeRelative(0,offset);offset+=it.height+space } }
        }
    }
}

@Composable internal fun AzChip(selected:Boolean,onClick:()->Unit,label:@Composable ()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true) {
    val c=Az.colors;val interaction=remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState();val focused by interaction.collectIsFocusedAsState()
    val bg=when { !enabled->c["tint-neutral"];selected->c[if(pressed)"primary-hover" else "primary"];pressed->c["bg-hover"];else->Color.Transparent }
    val fg=c[if(!enabled)"text-disabled" else if(selected)"on-primary" else "text"]
    Box(modifier.defaultMinSize(48.dp,48.dp).selectable(selected,interactionSource=interaction,indication=null,enabled=enabled,role=Role.RadioButton,onClick=onClick),contentAlignment=Alignment.Center) {
        Row(Modifier.heightIn(min=32.dp).focusRing(focused&&LocalInputModeManager.current.inputMode==InputMode.Keyboard,pill,c["focus"])
            .background(bg,pill).then(if(!selected&&enabled)Modifier.border(1.dp,c["line-control"],pill) else Modifier)
            .padding(horizontal=16.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            CompositionLocalProvider(LocalContentColor provides fg) { ProvideTextStyle(AzType.control) {
                if(selected) AzIcon(R.drawable.ic_az_check,18.dp)
                label()
            } }
        }
    }
}

@Composable internal fun AzTextField(value:String,onValueChange:(String)->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,
    label:@Composable ()->Unit,fieldLabel:String?=null,supportingText:(@Composable ()->Unit)?=null,singleLine:Boolean=true,
    keyboardOptions:KeyboardOptions=KeyboardOptions.Default,keyboardActions:KeyboardActions=KeyboardActions.Default,
    visualTransformation:VisualTransformation=VisualTransformation.None) {
    val c=Az.colors;val interaction=remember { MutableInteractionSource() };val focused by interaction.collectIsFocusedAsState()
    // The modifier belongs to the editable node, retaining keyboard and test semantics.
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        CompositionLocalProvider(LocalContentColor provides c["text"]) { ProvideTextStyle(AzType.label) { label() } }
        BasicTextField(value,onValueChange,modifier.semantics { fieldLabel?.let { contentDescription=it } }.fillMaxWidth().heightIn(min=48.dp)
            .background(c[if(enabled)"bg-control" else "tint-neutral"],RoundedCornerShape(6.dp))
            .border(if(focused)2.dp else 1.dp,c[if(!enabled)"line" else if(focused)"focus" else "line-control"],RoundedCornerShape(6.dp))
            .padding(horizontal=16.dp,vertical=8.dp),enabled=enabled,textStyle=AzType.body.copy(color=c[if(enabled)"text" else "text-disabled"]),
            keyboardOptions=keyboardOptions,keyboardActions=keyboardActions,singleLine=singleLine,visualTransformation=visualTransformation,
            interactionSource=interaction,cursorBrush=SolidColor(c["primary-text"]),
            decorationBox={ inner -> Box(contentAlignment=Alignment.CenterStart,modifier=Modifier.fillMaxWidth()) { inner() } })
        supportingText?.let { CompositionLocalProvider(LocalContentColor provides c["text-subtle"]) { ProvideTextStyle(AzType.label,it) } }
    }
}

@Composable internal fun AzCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().background(Az.colors["bg-card"],RoundedCornerShape(16.dp))
        .border(1.dp,Az.colors["line"],RoundedCornerShape(16.dp)).padding(Az.inset),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)
}

internal enum class AzStatus { NEUTRAL, INFO, LISTENING, SUCCESS, DANGER }
@Composable internal fun AzStatusLabel(text:String,icon:Int,status:AzStatus=AzStatus.NEUTRAL,modifier:Modifier=Modifier) {
    val c=Az.colors
    val token=when(status) { AzStatus.NEUTRAL->"neutral";AzStatus.INFO,AzStatus.LISTENING->"info";AzStatus.SUCCESS->"success";AzStatus.DANGER->"danger" }
    val strong=status==AzStatus.LISTENING
    val background=c[(if(strong)"status-" else "tint-")+token]
    val foreground=c[(if(strong)"on-" else "on-tint-")+token]
    Row(modifier.semantics(mergeDescendants=true) { liveRegion=LiveRegionMode.Polite }
        .background(background,pill).padding(horizontal=8.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        CompositionLocalProvider(LocalContentColor provides foreground) { AzIcon(icon,16.dp);Text(text,style=AzType.label) }
    }
}

@Composable internal fun AzDialog(onDismissRequest:()->Unit,title:@Composable ()->Unit,text:@Composable ()->Unit,
    confirmButton:@Composable ()->Unit,dismissButton:@Composable ()->Unit) {
    val c=Az.colors
    Dialog(onDismissRequest,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        // Compose's platform dim is replaced with the exact backdrop token.
        val view=LocalView.current
        SideEffect { (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window?.setDimAmount(0f) }
        Box(Modifier.fillMaxSize().background(c["backdrop"]).pointerInput(onDismissRequest) { detectTapGestures(onTap={onDismissRequest()}) }
            .windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),contentAlignment=Alignment.Center) {
            Column(Modifier.widthIn(max=560.dp).fillMaxWidth().shadow(12.dp,RoundedCornerShape(24.dp))
                .background(c["bg-floating"],RoundedCornerShape(24.dp))
                .pointerInput(Unit) { detectTapGestures(onTap={}) }
                .verticalScroll(rememberScrollState()).padding(24.dp)) {
                ProvideTextStyle(AzType.dialog.copy(color=c["text"])) { title() }
                Spacer(Modifier.height(16.dp));ProvideTextStyle(AzType.body.copy(color=c["text"])) { text() }
                Spacer(Modifier.height(24.dp))
                AzActions(gap=16.dp) { confirmButton();dismissButton() }
            }
        }
    }
}
