// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app.design

import android.app.Activity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import dev.openautoai.app.R

internal object AzType {
    val serifItalic = FontFamily(Font(R.font.dm_serif_display_italic,
        FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic))
    private val sans = FontFamily(Font(R.font.dm_sans_regular, FontWeight.Normal),
        Font(R.font.dm_sans_medium, FontWeight.Medium), Font(R.font.dm_sans_semibold, FontWeight.SemiBold))
    private fun style(size: Int, height: Int, weight: FontWeight) = TextStyle(fontFamily = sans,
        fontSize = size.sp, lineHeight = height.sp, fontWeight = weight, letterSpacing = 0.sp,
        hyphens = androidx.compose.ui.text.style.Hyphens.None,
        platformStyle = PlatformTextStyle(includeFontPadding = false))
    val title = style(22,28,FontWeight.SemiBold)
    val dialog = style(20,26,FontWeight.SemiBold)
    val section = style(17,22,FontWeight.SemiBold)
    val body = style(15,22,FontWeight.Normal)
    val control = style(15,20,FontWeight.Medium)
    val label = style(13,18,FontWeight.Medium)
}
private val LocalPalette = staticCompositionLocalOf { AzLight }
private val LocalSpace = staticCompositionLocalOf { 16.dp }
internal object Az {
    val colors: AzPalette @Composable get() = LocalPalette.current
    val inset: Dp @Composable get() = LocalSpace.current
}

@Composable
internal fun AzentriusTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val c = if(dark) AzDark else AzLight
    val base = if(dark) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(primary=c["primary"], onPrimary=c["on-primary"],
        background=c["bg-page"], surface=c["bg-page"], onBackground=c["text"], onSurface=c["text"],
        onSurfaceVariant=c["text-subtle"], surfaceContainerLow=c["bg-card"], surfaceContainer=c["bg-card"],
        surfaceContainerHigh=c["bg-floating"], surfaceContainerHighest=c["bg-floating"],
        surfaceTint=c["bg-floating"], outline=c["line-control"], outlineVariant=c["line"],
        error=c["status-danger"], onError=c["on-danger"], scrim=c["backdrop"])
    val view = LocalView.current
    SideEffect {
        (view.context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle=SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT){dark},
            navigationBarStyle=SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT){dark})
        (view.context as? Activity)?.window?.isNavigationBarContrastEnforced=false
    }
    val type = Typography(displayLarge=AzType.title,displayMedium=AzType.title,displaySmall=AzType.title,
        headlineLarge=AzType.title,headlineMedium=AzType.title,headlineSmall=AzType.title,
        titleLarge=AzType.section,titleMedium=AzType.section,titleSmall=AzType.section,
        bodyLarge=AzType.body,bodyMedium=AzType.body,bodySmall=AzType.label,
        labelLarge=AzType.control,labelMedium=AzType.label,labelSmall=AzType.label)
    BoxWithConstraints {
        CompositionLocalProvider(LocalPalette provides c, LocalContentColor provides c["text"], LocalSpace provides if(maxWidth<600.dp) 16.dp else 24.dp) {
            MaterialTheme(colorScheme=scheme,typography=type,content=content)
        }
    }
}
