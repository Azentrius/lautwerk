// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.prototype

import android.Manifest
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Opt-in access audit under the real app UID; no shell identity, launch, content or permission grant. */
class AssistantControlAccessDeviceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val targets = listOf("com.openai.chatgpt", "com.anthropic.claude")

    private fun requireOptIn() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("assistantControlsAudit") == "true")
        assumeTrue(Build.VERSION.SDK_INT >= 37)
        assertEquals("Audit must run under the target app UID", context.applicationInfo.uid, Process.myUid())
    }

    @Test fun ordinaryAppCannotQueryAnotherAppsShortcuts() {
        requireOptIn()
        val launcher = context.getSystemService(LauncherApps::class.java)
        assertFalse("This audit requires the unchanged non-assistant, non-launcher configuration",
            launcher.hasShortcutHostPermission())
        for ((index, target) in targets.withIndex()) {
            val query = LauncherApps.ShortcutQuery().setPackage(target).setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            expectDenied("shortcut", index) {
                // Never inspect labels, intents or contents if a future platform permits the query.
                launcher.getShortcuts(query, Process.myUserHandle())
            }
        }
    }

    @Test fun ordinaryAppCannotGetAnotherAppsMediaSessionTokens() {
        requireOptIn()
        assertEquals(PackageManager.PERMISSION_DENIED,
            context.checkSelfPermission(Manifest.permission.MEDIA_CONTENT_CONTROL))
        val sessions = context.getSystemService(MediaSessionManager::class.java)
        for ((index, target) in targets.withIndex()) {
            expectDenied("media", index) {
                // Package-specific Android 17 API; no notification listener and no token retained.
                sessions.getActiveSessionsForPackage(target, null)
            }
        }
    }

    private fun expectDenied(route: String, targetIndex: Int, call: () -> Any?) {
        try {
            call()
            fail("Expected access denial for $route target $targetIndex")
        } catch (_: SecurityException) {
            Log.i("Lautwerk", "control.audit route=$route target=$targetIndex result=DENIED")
        }
    }
}
