package dev.openautoai.prototype

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.TextView
import dev.openautoai.core.PrototypeSession

class MainActivity : Activity() {
    private lateinit var session: PrototypeSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = PrototypeSession(savedInstanceState?.getInt("starts") ?: 0)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { view, insets ->
            // Native insets keep the small phone UI usable on Android 15/16/17.
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        findViewById<TextView>(R.id.variant_note).setText(
            if (BuildConfig.DHU_EXPERIMENT) R.string.dhu_note else R.string.phone_note
        )
        findViewById<Button>(R.id.start).setOnClickListener {
            session.start()
            renderStatus()
            Log.i("OpenAutoAI", "phone.start count=${session.startCount}")
        }
        renderStatus()
        Log.i("OpenAutoAI", "phone.onCreate dhu=${BuildConfig.DHU_EXPERIMENT}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("starts", session.startCount)
        super.onSaveInstanceState(outState)
    }

    private fun renderStatus() {
        findViewById<TextView>(R.id.status).text = if (session.startCount == 0)
            getString(R.string.ready) else getString(R.string.started, session.startCount)
    }
}
