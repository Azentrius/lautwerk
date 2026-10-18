// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.storage

import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

internal interface ArchiveFile {
    val identity: String
    fun read(): ByteArray?
    fun write(bytes: ByteArray)
}

internal class AtomicArchiveFile(private val base: File) : ArchiveFile {
    private val atomic = AtomicFile(base)
    override val identity: String = base.canonicalPath

    override fun read(): ByteArray? = try {
        // openRead also restores a legacy .bak file when the base file is missing.
        atomic.openRead().use { it.readBytes() }
    } catch (error: FileNotFoundException) {
        if (base.exists() || File(base.path + ".bak").exists()) throw error
        null
    }

    override fun write(bytes: ByteArray) {
        val stream = atomic.startWrite()
        try {
            stream.write(bytes)
            atomic.finishWrite(stream)
        } catch (error: Exception) {
            atomic.failWrite(stream)
            throw error
        }
        // Some framework finalization failures are logged rather than thrown.
        if (!atomic.readFully().contentEquals(bytes)) throw IOException("Archive commit failed")
    }
}
