// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.controls

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import android.os.Build
import com.azentrius.lautwerk.car.handoff.ExperimentScreenFactory
import com.azentrius.lautwerk.car.handoff.OfficialAssistant

/** Public voice entries observed on this Pixel; no intents, extras or conversation shortcut use. */
class NativeShortcuts(private val context: Context) {
    private val launcher = context.getSystemService(LauncherApps::class.java)
    private val preferences = context.getSharedPreferences("native_control_shortcuts_v1", Context.MODE_PRIVATE)
    fun hasAccess(): Boolean = ExperimentScreenFactory.nativeControlsEnabled(context) &&
        runCatching { launcher.hasShortcutHostPermission() }.getOrDefault(false)
    fun locked(): Boolean = context.getSystemService(KeyguardManager::class.java).isDeviceLocked

    fun choices(assistant: OfficialAssistant): List<ShortcutInfo> {
        if (Build.VERSION.SDK_INT < 30 || !hasAccess() || locked()) return emptyList()
        return launcher.getShortcuts(LauncherApps.ShortcutQuery().setPackage(assistant.packageName)
            .setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC), Process.myUserHandle())
            .orEmpty().filter { it.`package` == assistant.packageName && it.userHandle == Process.myUserHandle() &&
                (it.isDeclaredInManifest || it.isDynamic) && it.isEnabled && it.locusId == null &&
                it.categories?.contains(ShortcutInfo.SHORTCUT_CATEGORY_CONVERSATION) != true &&
                allowedLabel(assistant, it.shortLabel?.toString().orEmpty()) }
    }

    @Suppress("DEPRECATION")
    private fun version(assistant: OfficialAssistant): Long =
        context.packageManager.getPackageInfo(assistant.packageName, 0).longVersionCode

    fun select(assistant: OfficialAssistant, choice: ShortcutInfo) {
        require(choices(assistant).any { it.id == choice.id })
        preferences.edit().putString(assistant.name, choice.id)
            .putLong("${assistant.name}.version", version(assistant)).apply()
    }

    fun configured(assistant: OfficialAssistant): Boolean = runCatching {
        preferences.contains(assistant.name) &&
            preferences.getLong("${assistant.name}.version", -1) == version(assistant)
    }.getOrDefault(false)

    fun start(assistant: OfficialAssistant): NativeResult = try {
        when {
            locked() -> NativeResult.LOCKED
            !hasAccess() -> NativeResult.NEEDS_ROLE
            !configured(assistant) -> NativeResult.NEEDS_SHORTCUT
            else -> {
                val id = preferences.getString(assistant.name, null)
                val choice = choices(assistant).singleOrNull { it.id == id }
                if (choice == null) NativeResult.NEEDS_SHORTCUT else {
                    launcher.startShortcut(choice, null, null)
                    NativeResult.START_REQUESTED
                }
            }
        }
    } catch (_: Exception) { NativeResult.BLOCKED }

    companion object {
        // Observed public launcher labels, not private IDs or undocumented component names.
        // Other localizations/labels require a new explicit compatibility observation.
        fun allowedLabel(assistant: OfficialAssistant, label: String): Boolean = when (assistant) {
            OfficialAssistant.CHATGPT -> label == "Audio"
            OfficialAssistant.CLAUDE -> label == "Sprachmodus starten"
        }
    }
}
