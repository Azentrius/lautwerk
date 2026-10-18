// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.app

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.azentrius.lautwerk.core.ai.RequestCancellation
import com.azentrius.lautwerk.core.storage.Conversation
import com.azentrius.lautwerk.core.storage.ConversationTextExport
import java.io.IOException
import java.io.OutputStream

internal class CreateChatDocument : ActivityResultContracts.CreateDocument(ConversationTextExport.MIME_TYPE) {
    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input).addCategory(Intent.CATEGORY_OPENABLE)
}

internal fun interface ConversationExportWriter {
    fun write(destination: Uri, chat: Conversation, cancellation: RequestCancellation)
}

/** Only the document returned by the user-driven picker; no paths, logs or persistent grant. */
internal class DocumentConversationExporter(private val open: (Uri) -> OutputStream?) : ConversationExportWriter {
    constructor(resolver: ContentResolver) : this({ uri -> resolver.openOutputStream(uri, "wt") })

    override fun write(destination: Uri, chat: Conversation, cancellation: RequestCancellation) {
        require(destination.scheme == ContentResolver.SCHEME_CONTENT)
        cancellation.check()
        val stream = open(destination) ?: throw IOException("Document unavailable")
        stream.writer(Charsets.UTF_8).buffered().use { writer ->
            ConversationTextExport.write(chat, writer, cancellation)
            cancellation.check()
            writer.flush()
        } // A failed flush or close is a failed export, never a success notification.
        cancellation.check()
    }
}
