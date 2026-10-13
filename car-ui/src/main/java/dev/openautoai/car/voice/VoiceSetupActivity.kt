// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.car.voice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.*
import android.text.InputType
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import dev.openautoai.car.R
import dev.openautoai.settings.ModelSelectionStore
import dev.openautoai.storage.EncryptedCredentialStore
import java.util.concurrent.Executors

/** Phone-only setup for the existing car package. Keys never cross package or IPC boundaries. */
class VoiceSetupActivity : Activity() {
    private lateinit var connection: VoiceConnection
    private lateinit var start: Button
    private lateinit var status: TextView
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var alive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        title = getString(R.string.voice_setup)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val inset = (28 * resources.displayMetrics.density).toInt() // Existing prototype phone spacing.
            setPadding(inset, inset, inset, inset)
        }
        val scroll = ScrollView(this).apply {
            // The launch window stays ink; the phone page follows its light/dark theme.
            val page = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, page, true)
            setBackgroundColor(page.data)
            addView(content)
        }
        scroll.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            }
            insets
        }
        setContentView(scroll)
        fun label(text: String) = TextView(this).apply { this.text = text; content.addView(this) }
        fun button(text: Int, action: () -> Unit) = Button(this).apply {
            setText(text); isAllCaps = false; setOnClickListener { action() }; content.addView(this)
        }
        label(getString(R.string.voice_setup_help))
        val preferences = VoicePreferences(this)
        val modes = RadioGroup(this)
        val names = listOf(R.string.voice_mode_playback, R.string.voice_mode_microphone, R.string.voice_mode_conversation)
        VoiceMode.entries.forEachIndexed { index, mode ->
            modes.addView(RadioButton(this).apply {
                id = View.generateViewId(); setText(names[index]); tag = mode
                isChecked = preferences.mode == mode
            })
        }
        content.addView(modes)
        modes.setOnCheckedChangeListener { group, checked ->
            preferences.mode = group.findViewById<RadioButton>(checked).tag as VoiceMode
        }
        button(R.string.voice_permissions) {
            val requested = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33) requested.add(Manifest.permission.POST_NOTIFICATIONS)
            requestPermissions(requested.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }.toTypedArray(), 21)
        }
        val selected = ModelSelectionStore(this).load()
        label(getString(R.string.voice_key_label, selected.provider.displayName))
        val key = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSaveEnabled = false
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
            privateImeOptions = "nm"
            hint = getString(R.string.voice_key_hint)
        }
        content.addView(key)
        val keyStatus = label(getString(R.string.voice_key_separate))
        button(R.string.voice_key_save) {
            val secret = key.text.toString().trim()
            key.text.clear()
            if (secret.isBlank()) { keyStatus.setText(R.string.voice_key_empty); return@button }
            worker.execute {
                val saved = runCatching { EncryptedCredentialStore(this).save(selected.provider, secret) }.isSuccess
                main.post { if (alive) keyStatus.setText(if (saved) R.string.voice_key_saved else R.string.voice_key_failed) }
            }
        }
        button(R.string.voice_key_clear) {
            key.text.clear()
            worker.execute {
                val cleared = runCatching { EncryptedCredentialStore(this).clear(selected.provider) }.isSuccess
                main.post { if (alive) keyStatus.setText(if (cleared) R.string.voice_key_cleared else R.string.voice_key_failed) }
            }
        }
        status = label(getString(R.string.car_ready)).apply { accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        connection = VoiceConnection(this)
        start = button(R.string.voice_phone_start) { connection.start() }
        label(getString(R.string.voice_phone_help))
    }
    override fun onStart() {
        super.onStart()
        connection.connect {
            status.text = connection.state.status.ifBlank { getString(R.string.car_ready) }
            start.setText(if (connection.state.active) R.string.voice_stop else R.string.voice_phone_start)
            val endsSession = connection.state.active
            start.setOnClickListener { if (endsSession) connection.stop() else connection.start() }
        }
    }
    override fun onStop() { connection.close(); super.onStop() }
    override fun onDestroy() { alive = false; worker.shutdownNow(); main.removeCallbacksAndMessages(null); super.onDestroy() }
}
