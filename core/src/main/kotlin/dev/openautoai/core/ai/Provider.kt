// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.core.ai

enum class Provider(val displayName: String, val keyId: String) {
    OPENAI("ChatGPT / OpenAI", "openai"),
    CLAUDE("Claude / Anthropic", "anthropic")
}

data class ModelOption(val id: String, val label: String, val provider: Provider)

/** Versioned starter catalog, retained from 0.1.0. API access depends on the account. */
object ModelCatalog {
    val options = listOf(
        ModelOption("gpt-5.4-mini", "GPT-5.4 mini", Provider.OPENAI),
        ModelOption("gpt-5.4", "GPT-5.4", Provider.OPENAI),
        ModelOption("claude-sonnet-4-6", "Claude Sonnet 4.6", Provider.CLAUDE),
        ModelOption("claude-opus-4-6", "Claude Opus 4.6", Provider.CLAUDE),
        ModelOption("claude-haiku-4-5", "Claude Haiku 4.5", Provider.CLAUDE)
    )
    fun forProvider(provider: Provider): List<ModelOption> = options.filter { it.provider == provider }
    fun default(provider: Provider): String = forProvider(provider).first().id
    fun valid(provider: Provider, id: String) = options.any { it.provider == provider && it.id == id }
}
