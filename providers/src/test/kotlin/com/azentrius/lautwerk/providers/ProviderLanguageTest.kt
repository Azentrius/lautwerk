// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.providers

import com.azentrius.lautwerk.core.ai.*
import com.azentrius.lautwerk.core.i18n.AppLanguage
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class ProviderLanguageTest {
    @Test fun bothProvidersReceiveMatchingLanguageWithoutChangingConversationOrBudget() {
        val old=AppLanguage.preferences
        try {
            for (locale in listOf(Locale.GERMAN,Locale.ENGLISH,Locale.JAPANESE)) {
                AppLanguage.preferences={listOf(locale)}
                for (provider in Provider.entries) {
                    val request=ChatRequest(ModelSelection(provider,ModelCatalog.default(provider)),listOf(ChatMessage("user","Unveränderter Text")))
                    val adapter=if(provider==Provider.OPENAI) OpenAiAdapter() else ClaudeAdapter()
                    val http=adapter.request(request,"dummy")
                    val json=JSONObject(http.body)
                    val instruction=json.getString(if(provider==Provider.OPENAI) "instructions" else "system")
                    assertTrue(instruction.contains(if(locale.language=="de") "deutschsprachiger" else "English-speaking"))
                    assertEquals(request.selection.modelId,json.getString("model"))
                    assertEquals(350,json.getInt(if(provider==Provider.OPENAI) "max_output_tokens" else "max_tokens"))
                    assertEquals("Unveränderter Text",json.getJSONArray(if(provider==Provider.OPENAI) "input" else "messages").getJSONObject(0).getString("content"))
                }
            }
        } finally { AppLanguage.preferences=old }
    }
}
