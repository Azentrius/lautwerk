// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.core.ai

import org.junit.Assert.*
import org.junit.Test

class ModelSelectionTest {
    @Test fun legacyProviderIdentifiersAndDefaultsRemainStable() {
        assertEquals("openai", Provider.valueOf("OPENAI").keyId)
        assertEquals("anthropic", Provider.valueOf("CLAUDE").keyId)
        assertEquals("gpt-5.4-mini", ModelSelection.default().modelId)
        assertEquals("claude-sonnet-4-6", ModelSelection.default(Provider.CLAUDE).modelId)
        assertEquals(5, ModelCatalog.options.map { it.id }.toSet().size)
    }

    @Test fun rejectsCrossProviderAndUnknownModels() {
        assertThrows(IllegalArgumentException::class.java) { ModelSelection(Provider.OPENAI, "claude-opus-4-6") }
        assertThrows(IllegalArgumentException::class.java) { ModelSelection(Provider.CLAUDE, "gpt-5.4") }
        assertThrows(IllegalArgumentException::class.java) { ModelSelection(Provider.OPENAI, "removed-model") }
    }
}
