package dev.openautoai.app

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import dev.openautoai.core.ai.*
import dev.openautoai.core.storage.*
import dev.openautoai.settings.ModelSelectionStore
import org.junit.Assert.*
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class HomeTestFixture {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val name = "home-test-${UUID.randomUUID()}"
    private val owner = ViewModelStore()
    val onMain = AtomicBoolean(false)
    val archive = Archive { checkThread() }
    val keys = Keys { checkThread() }
    val exports = Exports { checkThread() }
    val selections = ModelSelectionStore(context.getSharedPreferences(name, Context.MODE_PRIVATE))
    lateinit var home: HomeViewModel
    private fun checkThread() { if (Looper.myLooper() == Looper.getMainLooper()) onMain.set(true) }
    fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    fun create() = main {
        AppState.update(false, Provider.OPENAI, ModelCatalog.default(Provider.OPENAI), null, "Bereit")
        AppState.reportIssue(null)
        home = HomeViewModel(selections, keys, archive, exports)
        owner.put("home", home)
    }
    fun close() {
        main { owner.clear(); AppState.update(false, Provider.OPENAI, ModelCatalog.default(Provider.OPENAI), null, "Bereit"); AppState.reportIssue(null) }
        context.deleteSharedPreferences(name)
        assertFalse("Encrypted store work ran on the UI thread", onMain.get())
    }
    fun await(test: () -> Boolean) {
        val end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (System.nanoTime() < end) {
            var ready = false
            main { ready = test() }
            if (ready) return
            Thread.sleep(20)
        }
        fail("Timed out waiting for phone state")
    }

    class Gate {
        val entered = CountDownLatch(1)
        private val released = CountDownLatch(1)
        fun block() {
            entered.countDown()
            while (released.count > 0) {
                try { check(released.await(10, TimeUnit.SECONDS)) { "test gate timed out" } }
                catch (_: InterruptedException) { /* Simulate a slow non-interruptible store. */ }
            }
        }
        fun open() = released.countDown()
    }
    class Archive(private val checkThread: () -> Unit) : ConversationRepository {
        @Volatile var items = emptyList<Conversation>()
        @Volatile var failRead = false
        @Volatile var beforeGet: (() -> Unit)? = null
        @Volatile var beforeAll: (() -> Unit)? = null
        val reads = AtomicInteger()
        val creates = AtomicInteger()
        val deletes = AtomicInteger()
        override fun all(): List<Conversation> {
            checkThread(); reads.incrementAndGet()
            val snapshot = items
            beforeAll?.invoke()
            if (failRead) throw StorageFailure(StorageError.ARCHIVE_READ)
            return snapshot
        }
        override fun get(id: String): Conversation? { checkThread(); beforeGet?.invoke(); return items.find { it.id == id } }
        override fun create(provider: Provider, modelId: String): Conversation {
            checkThread()
            val chat = Conversation("created-${creates.incrementAndGet()}", "Test", provider, modelId, 0, emptyList())
            items = items + chat
            return chat
        }
        override fun append(id: String, turn: Turn) { checkThread() }
        override fun delete(id: String) { checkThread(); deletes.incrementAndGet(); items = items.filterNot { it.id == id } }
    }
    class Exports(private val checkThread: () -> Unit) : ConversationExportWriter {
        val writes = AtomicInteger()
        @Volatile var chat: Conversation? = null
        @Volatile var beforeWrite: (() -> Unit)? = null
        @Volatile var failure: Exception? = null
        override fun write(destination: Uri, chat: Conversation, cancellation: RequestCancellation) {
            checkThread(); writes.incrementAndGet(); beforeWrite?.invoke(); cancellation.check()
            failure?.let { throw it }
            this.chat = chat
        }
    }
    class Keys(private val checkThread: () -> Unit) : CredentialStore {
        @Volatile var failRead = false
        @Volatile var failClear = false
        @Volatile var beforeRead: (() -> Unit)? = null
        @Volatile var beforeSave: (() -> Unit)? = null
        val reads = AtomicInteger()
        val saves = AtomicInteger()
        val clears = AtomicInteger()
        @Volatile var savedProvider: Provider? = null
        override fun read(provider: Provider): String? {
            checkThread(); reads.incrementAndGet(); beforeRead?.invoke()
            if (failRead) throw StorageFailure(StorageError.CREDENTIAL_READ)
            return "dummy-key"
        }
        override fun save(provider: Provider, secret: String) {
            checkThread(); beforeSave?.invoke(); saves.incrementAndGet(); savedProvider = provider
        }
        override fun clear(provider: Provider) {
            checkThread()
            if (failClear) throw StorageFailure(StorageError.CREDENTIAL_WRITE)
            clears.incrementAndGet()
        }
    }
}
