// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car.controls

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.azentrius.lautwerk.car.R
import com.azentrius.lautwerk.car.handoff.ExperimentScreenFactory
import com.azentrius.lautwerk.car.handoff.OfficialAssistant

/** Temporary framework UI, opened deliberately on the phone. No automatic permission requests. */
class NativeControlSetupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ExperimentScreenFactory.nativeControlsEnabled(this)) { finish(); return }
        title = getString(R.string.native_control_title)
    }

    override fun onResume() {
        super.onResume()
        if (!ExperimentScreenFactory.nativeControlsEnabled(this)) return
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun text(id: Int) { layout.addView(TextView(this).apply { setText(id) }) }
        fun button(label: String, action: () -> Unit) {
            layout.addView(Button(this).apply { text = label; setOnClickListener { action() } })
        }
        text(R.string.native_setup_disclosure)
        button(getString(R.string.native_role_settings)) {
            confirm(R.string.native_role_disclosure) { openSettings(Settings.ACTION_VOICE_INPUT_SETTINGS) }
        }
        button(getString(R.string.native_listener_settings)) {
            confirm(R.string.native_listener_disclosure) { openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        }
        val shortcuts = NativeShortcuts(this)
        OfficialAssistant.entries.forEach { target ->
            button(getString(R.string.native_choose_shortcut, target.displayName())) {
                try {
                    if (!shortcuts.hasAccess()) message(R.string.native_needs_role)
                    else {
                        val choices = shortcuts.choices(target)
                        if (choices.isEmpty()) message(R.string.native_no_shortcuts) else
                            AlertDialog.Builder(this).setTitle(getString(R.string.native_choose_shortcut, target.displayName()))
                                .setItems(choices.map { it.shortLabel?.toString().orEmpty() }.toTypedArray()) { _, index ->
                                    try { shortcuts.select(target, choices[index]); message(R.string.native_shortcut_saved) }
                                    catch (_: Exception) { message(R.string.native_blocked) }
                                }.setNegativeButton(android.R.string.cancel, null).show()
                    }
                } catch (_: Exception) { message(R.string.native_blocked) }
            }
        }
        button(getString(R.string.native_check_setup)) {
            val controls = NativeControls.port(this)
            AlertDialog.Builder(this).setMessage(OfficialAssistant.entries.joinToString("\n") {
                "${it.displayName()}: ${getString(controls.readiness(it).message())}"
            }).setPositiveButton(android.R.string.ok, null).show()
        }
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun confirm(text: Int, action: () -> Unit) {
        AlertDialog.Builder(this).setMessage(text).setPositiveButton(R.string.native_continue) { _, _ -> action() }
            .setNegativeButton(android.R.string.cancel, null).show()
    }
    private fun openSettings(action: String) {
        try { startActivity(Intent(action)) } catch (_: Exception) { message(R.string.native_blocked) }
    }
    private fun message(text: Int) { AlertDialog.Builder(this).setMessage(text).setPositiveButton(android.R.string.ok, null).show() }
}
