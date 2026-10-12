// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.prototype

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import dev.openautoai.core.ai.ModelCatalog
import dev.openautoai.core.ai.ModelOption
import dev.openautoai.core.ai.ModelSelection
import dev.openautoai.core.ai.Provider
import dev.openautoai.settings.ModelSelectionStore
import java.io.Closeable
import android.widget.Button
import android.widget.TextView
import dev.openautoai.core.PrototypeSession

class MainActivity : Activity() {
    private lateinit var session: PrototypeSession
    private lateinit var selections: ModelSelectionStore
    private lateinit var providerSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private var displayedProvider: Provider? = null
    private var models = emptyList<ModelOption>()
    private var subscription: Closeable? = null

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
        configureSelection()
        renderStatus()
        Log.i("OpenAutoAI", "phone.onCreate dhu=${BuildConfig.DHU_EXPERIMENT}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("starts", session.startCount)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        subscription = selections.observe(::renderSelection)
    }

    override fun onStop() {
        subscription?.close()
        subscription = null
        super.onStop()
    }

    private fun configureSelection() {
        selections = ModelSelectionStore(this)
        providerSpinner = findViewById(R.id.provider)
        modelSpinner = findViewById(R.id.model)
        providerSpinner.adapter = adapter(Provider.entries.map { it.displayName })
        renderSelection(selections.load())
        providerSpinner.onItemSelectedListener = onSelection { position ->
            // Spinner notifications can be queued during binding: ignore stale positions.
            if (providerSpinner.selectedItemPosition == position) {
                val provider = Provider.entries[position]
                if (selections.load().provider != provider) selections.selectProvider(provider)
            }
        }
        modelSpinner.onItemSelectedListener = onSelection { position ->
            val model = models.getOrNull(position)
            if (modelSpinner.selectedItemPosition == position && model != null &&
                selections.load().provider == model.provider) {
                val next = ModelSelection(model.provider, model.id)
                if (selections.load() != next) selections.select(next)
            }
        }
    }

    private fun renderSelection(selection: ModelSelection) {
        providerSpinner.setSelection(selection.provider.ordinal)
        if (displayedProvider != selection.provider) {
            displayedProvider = selection.provider
            models = ModelCatalog.forProvider(selection.provider)
            modelSpinner.adapter = adapter(models.map { it.label })
        }
        modelSpinner.setSelection(models.indexOfFirst { it.id == selection.modelId })
        findViewById<TextView>(R.id.selection_status).text = getString(
            R.string.selection_saved, selection.provider.displayName, selection.model.label
        )
    }

    private fun adapter(items: List<String>) = ArrayAdapter(
        this, android.R.layout.simple_spinner_item, items
    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

    private fun onSelection(onSelected: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onSelected(position)
        }
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private fun renderStatus() {
        findViewById<TextView>(R.id.status).text = if (session.startCount == 0)
            getString(R.string.ready) else getString(R.string.started, session.startCount)
    }
}
