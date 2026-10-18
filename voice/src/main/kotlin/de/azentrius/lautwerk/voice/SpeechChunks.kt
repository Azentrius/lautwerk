// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.voice

/** TTS limits use UTF-16 length; never split a surrogate pair or send an oversized utterance. */
object SpeechChunks {
    fun split(text: String, limit: Int): List<String> {
        require(limit >= 2)
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            var end = minOf(start + limit, text.length)
            if (end < text.length && text[end - 1].isHighSurrogate()) end--
            if (end < text.length) {
                val space = text.lastIndexOf(' ', end - 1)
                if (space > start + limit / 2) end = space + 1
            }
            val part = text.substring(start, end)
            if (part.isNotBlank()) chunks += part
            start = end
        }
        return chunks
    }
}
