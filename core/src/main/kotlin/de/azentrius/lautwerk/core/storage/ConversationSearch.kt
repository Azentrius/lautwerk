// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.core.storage

import dev.openautoai.core.ai.Provider
import dev.openautoai.core.ai.RequestCancellation

data class ConversationMatch(val conversation: Conversation, val firstMatchingTurn: Int? = null)

/** Literal, case-insensitive substring search over an immutable in-memory archive snapshot. */
object ConversationSearch {
    const val MAX_QUERY_LENGTH = 200

    fun find(chats: List<Conversation>, query: String, provider: Provider?,
             cancellation: RequestCancellation = RequestCancellation.NONE): List<ConversationMatch> {
        val term = query.take(MAX_QUERY_LENGTH).trim()
        fun contains(value: String): Boolean {
            // Bound each scan so cancellation also works inside an unusually long message.
            var offset = 0
            while (offset < value.length) {
                cancellation.check()
                val end = minOf(offset + 4096 + term.length, value.length)
                if (value.substring(offset, end).contains(term, ignoreCase = true)) return true
                offset += 4096
            }
            return false
        }
        val matches = ArrayList<ConversationMatch>()
        cancellation.check()
        for (chat in chats) {
            cancellation.check()
            if (provider != null && chat.provider != provider) continue
            if (term.isEmpty() || contains(chat.title) || contains(chat.provider.displayName) || contains(chat.modelId)) {
                matches += ConversationMatch(chat)
                continue
            }
            for ((index, turn) in chat.turns.withIndex()) {
                cancellation.check()
                if (contains(turn.content)) {
                    matches += ConversationMatch(chat, index)
                    break
                }
            }
        }
        cancellation.check()
        return matches
    }
}
