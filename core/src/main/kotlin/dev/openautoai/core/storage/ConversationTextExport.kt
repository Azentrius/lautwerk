package dev.openautoai.core.storage

import dev.openautoai.core.ai.RequestCancellation
import java.io.Writer
import java.time.Instant

/** A readable copy, not a backup format. No credential store or external I/O here. */
object ConversationTextExport {
    const val FILE_NAME = "OpenAutoAI-Chat.txt"
    const val MIME_TYPE = "text/plain"

    fun write(chat: Conversation, output: Writer, cancellation: RequestCancellation = RequestCancellation.NONE) {
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
        text("OpenAuto AI – Gesprächsexport\nUnverschlüsselte Textkopie; kein Wiederherstellungsformat.\n\n")
        text("Titel:\n"); text(chat.title)
        text("\n\nAnbieter: "); text(chat.provider.displayName)
        text("\nModell: "); text(chat.modelId)
        text("\nZuletzt geändert (UTC): ${Instant.ofEpochMilli(chat.updatedAt)}")
        text("\nNachrichten: ${chat.turns.size}\n")
        chat.turns.forEachIndexed { index, turn ->
            val role = when (turn.role) { "user" -> "Du"; "assistant" -> "KI"; else -> turn.role }
            text("\n--- Nachricht ${index + 1} · "); text(role); text(" ---\n")
            text(turn.content); text("\n")
        }
        cancellation.check()
    }
}
