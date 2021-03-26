package gma.logs.app.controller.logsinstance

import gma.commons.fx.subscribeWithExceptionHandler
import gma.logs.app.model.query.LogEntry
import gma.logs.app.model.query.LogRepository
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections.emptyObservableList
import javafx.collections.ObservableList
import javafx.util.Duration
import mu.KotlinLogging
import tornadofx.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class ResultsController : Controller() {

    override val scope = super.scope as LogsScope

    val resultsProperty = SimpleObjectProperty<ObservableList<LogEntry<*>>>()

    val hitCountProperty = resultsProperty.select { it.sizeProperty } as Property<Int?>

    private val _knownKeys = observableListOf(Key.rawEntry)
    val knownKeys = _knownKeys.asUnmodifiable()

    val multilineProperty = booleanProperty()
    var isMultiline by multilineProperty

    val timestampFormatProperty = objectProperty(TimestampFormat.LOCAL_DATE_TIME)
    var timestampFormat: TimestampFormat by timestampFormatProperty

    val loadingProperty = booleanProperty()
    var isLoading by loadingProperty

    enum class TimestampFormat(val formatter: DateTimeFormatter) {
        LOCAL_TIME(DateTimeFormatterBuilder().appendPattern("HH:mm:ss.SSS").toFormatter()),
        LOCAL_DATE_TIME(
            DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ').append(LOCAL_TIME.formatter).toFormatter()
        )
    }

    class LogRepositoryRequest(val query: LogRepository.LogQuery, override val scope: Scope) :
        FXEvent(EventBus.RunOn.BackgroundThread)

    class LogRepositoryResponse(
        val result: ObservableList<LogEntry<*>>,
        val sortedFields: List<String>,
        override val scope: Scope
    ) : FXEvent()

    init {
        scope.queryBinding.onChange { query ->
            query?.let {
                fire(LogRepositoryRequest(query, scope))
            }
        }

        subscribeWithExceptionHandler<LogRepositoryRequest> { event ->
            val count: Int
            val startTime = System.currentTimeMillis()
            var firstEntryDelay: Long? = null
            val done = AtomicBoolean(false)
            runLater(Duration.millis(100.0)) {
                if (!done.get()) {
                    isLoading = true
                }
            }
            try {
                val fields = HashSet<String>()
                val list = ArrayList<LogEntry<*>>()
                scope.logRepository.query(event.query) {
                    if (firstEntryDelay == null) {
                        firstEntryDelay = System.currentTimeMillis() - startTime
                    }
                    if (list.size == 10_000) {
                        // Clears the previous results after a few entries were processed to let the GC free the memory
                        fire(LogRepositoryResponse(emptyObservableList(), emptyList(), scope))
                    }
                    list.add(it)
                    fields.addAll(it.keys)
                }
                count = list.size
                fire(LogRepositoryResponse(list.asObservable(), fields.toList().sorted(), scope))
            } finally {
                done.set(true)
                runLater { isLoading = false }
            }
            logger.info { "Request processed: $count entries in ${System.currentTimeMillis() - startTime} ms ($firstEntryDelay ms to initial entry)" }
        }

        subscribeWithExceptionHandler<LogRepositoryResponse> { event ->
            merge(event.sortedFields, _knownKeys)
            resultsProperty.value = event.result
        }
    }

    private fun merge(src: List<String>, dst: MutableList<Key>) {
        var i = 0
        var j = 0
        while (i < src.size && j < dst.size) {
            when {
                src[i] < dst[j].name -> {
                    dst.add(j++, Key(src[i++]))
                }
                src[i] > dst[j].name -> {
                    j++
                }
                else -> {
                    i++
                    j++
                }
            }
        }
        while (i < src.size) {
            dst.add(Key(src[i++]))
        }
    }

    var visibleKeysNames: List<String>
        get() = knownKeys.filter(Key::isVisible).map(Key::name)
        set(value) {
            value.forEach { keyName ->
                _knownKeys.find { it.name == keyName }?.let { it.isVisible = true }
                    ?: _knownKeys.add(Key(keyName).apply { isVisible = true })
            }
            _knownKeys.filter { it.isVisible && it.name !in value }.forEach { it.isVisible = false }
            _knownKeys.sort()
        }


    data class Key constructor(val name: String, val isMeta: Boolean) : Comparable<Key> {
        constructor(name: String) : this(name, false)

        companion object {
            val rawEntry = Key(
                // These non printable characters should help with non meta field collision and sorting
                "\u0000_Raw Entry_\u200F\u200E",
                true
            ).apply {
                isVisible = true
            }
        }

        operator fun get(entry: LogEntry<*>) =
            when (this) {
                rawEntry -> entry.raw
                else -> entry[name]
            }

        override fun compareTo(other: Key) = name.compareTo(other.name)

        val visibleProperty = booleanProperty()
        var isVisible by visibleProperty
    }

}
