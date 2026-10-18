// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.core.storage

import com.azentrius.lautwerk.core.ai.RequestCancellation
import java.io.Writer
import java.time.Instant
import java.util.Locale
import com.azentrius.lautwerk.core.i18n.AppLanguage

/** A readable copy, not a backup format. No credential store or external I/O here. */
object ConversationTextExport {
    const val FILE_NAME = "Lautwerk-Chat.txt"
    const val MIME_TYPE = "text/plain"

    fun write(chat: Conversation, output: Writer, cancellation: RequestCancellation = RequestCancellation.NONE, locale: Locale = AppLanguage.locale()) {
        fun label(de: String, en: String) = if (locale.language == "de") de else en
        // Chunk even a single very long message; do not build a second full in-memory transcript.
        fun text(value: String) {
            var offset = 0
            while (offset < value.length) {
                cancellation.check()
                val count = minOf(4096, value.length - offset)
                output.write(value, offset, count)
                offset += count
            }
        }
        cancellation.check()
        text(label("Lautwerk – Gesprächsexport\nUnverschlüsselte Textkopie; kein Wiederherstellungsformat.\n\n", "Lautwerk – Conversation export\nUnencrypted text copy; not a backup format.\n\n"))
        text(label("Titel:\n", "Title:\n")); text(chat.title)
        text(label("\n\nAnbieter: ", "\n\nProvider: ")); text(chat.provider.displayName)
        text(label("\nModell: ", "\nModel: ")); text(chat.modelId)
        text(label("\nZuletzt geändert (UTC): ${Instant.ofEpochMilli(chat.updatedAt)}", "\nLast updated (UTC): ${Instant.ofEpochMilli(chat.updatedAt)}"))
        text(label("\nNachrichten: ${chat.turns.size}\n", "\nMessages: ${chat.turns.size}\n"))
        chat.turns.forEachIndexed { index, turn ->
            val role = when (turn.role) { "user" -> label("Du", "You"); "assistant" -> label("KI", "AI"); else -> turn.role }
            text(label("\n--- Nachricht ${index + 1} · ", "\n--- Message ${index + 1} · ")); text(role); text(" ---\n")
            text(turn.content); text("\n")
        }
        cancellation.check()
    }
}
