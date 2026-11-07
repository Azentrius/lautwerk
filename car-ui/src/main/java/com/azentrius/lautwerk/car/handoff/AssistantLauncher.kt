// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.handoff

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.azentrius.lautwerk.car.LautwerkCarService
import com.azentrius.lautwerk.car.R
import com.azentrius.lautwerk.voice.HandoffPhase

enum class OfficialAssistant(val packageName: String, val label: Int) {
    CHATGPT("com.openai.chatgpt", R.string.handoff_chatgpt),
    CLAUDE("com.anthropic.claude", R.string.handoff_claude)
}

fun interface AssistantLauncher { fun open(assistant: OfficialAssistant): HandoffPhase }

enum class AssistantEntry { LAUNCHER, ASSIST }

/** Public SDK intents only. No automatic fallback or vendor-specific components/extras. */
class AssistantIntentResolver(
    context: Context,
    private val entry: AssistantEntry,
    private val frontDoor: (String) -> Intent? = context.applicationContext.packageManager::getLaunchIntentForPackage,
    private val canAssist: (Intent) -> Boolean = { intent ->
        @Suppress("DEPRECATION")
        context.applicationContext.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .any { result ->
                val info = result.activityInfo
                info.packageName == intent.`package` && info.exported && info.enabled &&
                    (info.permission == null || context.checkSelfPermission(info.permission) == PackageManager.PERMISSION_GRANTED)
            }
    }
) {
    fun resolve(packageName: String): Intent? = when (entry) {
        AssistantEntry.LAUNCHER -> frontDoor(packageName)
        AssistantEntry.ASSIST -> Intent(Intent.ACTION_ASSIST).setPackage(packageName).takeIf(canAssist)
    }
}

/** Android may silently deny a background launch. Neither route acknowledges a voice session. */
class AndroidAssistantLauncher(
    context: Context,
    private val entry: AssistantEntry = HandoffExperiment.entry(context),
    private val locked: () -> Boolean = { context.applicationContext.getSystemService(KeyguardManager::class.java).isDeviceLocked },
    private val resolve: (String) -> Intent? = AssistantIntentResolver(context, entry)::resolve,
    // The destination belongs on the phone, not the CarContext's virtual display.
    private val dispatch: (Intent) -> Unit = context.applicationContext::startActivity
) : AssistantLauncher {
    override fun open(assistant: OfficialAssistant): HandoffPhase {
        var stage = "LOCK_CHECK"
        var failure = "NONE"
        val result = try {
            // ASSIST leaves lock handling to Android and the selected app. No keyguard dismissal,
            // show-when-locked flag, assistant role change or system service binding is requested.
            if (entry == AssistantEntry.LAUNCHER && locked()) HandoffPhase.LOCKED
            else {
                stage = "RESOLVE"
                val intent = resolve(assistant.packageName)
                if (intent == null) HandoffPhase.UNAVAILABLE else {
                    stage = "DISPATCH"
                    dispatch(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    HandoffPhase.REQUESTED // Not an acknowledgement from the destination app.
                }
            }
        } catch (_: ActivityNotFoundException) { failure = "NOT_FOUND"; HandoffPhase.UNAVAILABLE }
        catch (_: SecurityException) { failure = "SECURITY"; HandoffPhase.BLOCKED }
        catch (_: UnsupportedOperationException) { failure = "UNSUPPORTED"; HandoffPhase.BLOCKED }
        catch (_: Exception) { failure = "OTHER"; HandoffPhase.BLOCKED }
        Log.i(LautwerkCarService.TAG, "handoff.dispatch target=${assistant.name} entry=$entry result=$result stage=$stage failure=$failure")
        return result
    }
}

object HandoffExperiment {
    @Suppress("DEPRECATION")
    fun entry(context: Context): AssistantEntry = if (enabled(context) && runCatching {
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData?.getString("com.azentrius.lautwerk.car.HANDOFF_ENTRY") == "assist"
    }.getOrDefault(false)) AssistantEntry.ASSIST else AssistantEntry.LAUNCHER

    @Suppress("DEPRECATION")
    fun enabled(context: Context): Boolean = runCatching {
        val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 &&
            info.metaData?.getBoolean("com.azentrius.lautwerk.car.HANDOFF_EXPERIMENT", false) == true
    }.getOrDefault(false)
}
