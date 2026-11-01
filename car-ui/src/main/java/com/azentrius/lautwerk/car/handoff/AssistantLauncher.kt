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

/** Public front-door intent only. Android may silently deny a background launch. */
class AndroidAssistantLauncher(
    context: Context,
    private val locked: () -> Boolean = { context.applicationContext.getSystemService(KeyguardManager::class.java).isDeviceLocked },
    private val resolve: (String) -> Intent? = context.applicationContext.packageManager::getLaunchIntentForPackage,
    // The destination belongs on the phone, not the CarContext's virtual display.
    private val dispatch: (Intent) -> Unit = context.applicationContext::startActivity
) : AssistantLauncher {
    override fun open(assistant: OfficialAssistant): HandoffPhase {
        var stage = "LOCK_CHECK"
        var failure = "NONE"
        val result = try {
            if (locked()) HandoffPhase.LOCKED
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
        Log.i(LautwerkCarService.TAG, "handoff.dispatch target=${assistant.name} result=$result stage=$stage failure=$failure")
        return result
    }
}

object HandoffExperiment {
    @Suppress("DEPRECATION")
    fun enabled(context: Context): Boolean = runCatching {
        val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 &&
            info.metaData?.getBoolean("com.azentrius.lautwerk.car.HANDOFF_EXPERIMENT", false) == true
    }.getOrDefault(false)
}
