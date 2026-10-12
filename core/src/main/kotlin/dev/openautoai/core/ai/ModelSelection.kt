// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.core.ai

/** A provider/model pair is valid as a unit; never silently route it to another provider. */
data class ModelSelection(val provider: Provider, val modelId: String) {
    init {
        require(ModelCatalog.valid(provider, modelId)) { "Modell gehört nicht zum gewählten Anbieter" }
    }

    val model: ModelOption
        get() = ModelCatalog.forProvider(provider).first { it.id == modelId }

    companion object {
        fun default(provider: Provider = Provider.OPENAI) = ModelSelection(provider, ModelCatalog.default(provider))
    }
}
