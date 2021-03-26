package gma.logs.logexplorer.search

import com.tsquare.logexplorer.backend.BackendConfiguration
import com.tsquare.logexplorer.backend.BackendFactory
import com.tsquare.logexplorer.config.ConfigurationBuilder
import com.tsquare.logexplorer.config.YamlConfiguration
import com.tsquare.logexplorer.search.QueryDateRange
import com.tsquare.logexplorer.search.SearchResult
import com.tsquare.logexplorer.search.query.QuerySpecification
import gma.logs.app.model.query.LogEntry
import gma.logs.app.model.query.LogRepository
import gma.logs.logexplorer.config.LogExplorerConfiguration
import java.io.Closeable
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val timestampFormatterDigit = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX")!!
private val timestampFormatterLetter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")!!
val SearchResult.Attributes.timestamp: ZonedDateTime
    get() = (get("@timestamp") as String).let {
        if (it.last() in '0'..'9') ZonedDateTime.parse(it, timestampFormatterDigit)
        else ZonedDateTime.parse(it, timestampFormatterLetter)
    }

interface ProgressHandler {

    fun start(name: String)

    fun add(progress: Long?, total: Long?)

    fun error(e: Exception)

    fun done()
}

typealias ResultHandler = (result: SearchResult) -> Unit

class Environment(val configuration: LogExplorerConfiguration) : Closeable {

    val profiles: LinkedHashSet<String>
        get() = LinkedHashSet(configuration.profileList)

    val configFile = File(configuration.configurationFile)

    private val yamlConfiguration = YamlConfiguration(configFile.toURI(), profiles, false)

    private val internalConfiguration by lazy {
        ConfigurationBuilder().chain(yamlConfiguration).build()
    }

    private val backend by lazy {
        val backendConfiguration = BackendConfiguration()
            .backendUris(internalConfiguration.backendUris())
            .connectTimeout(internalConfiguration.connectTimeout())
            .socketTimeout(internalConfiguration.socketTimeout())
            .authenticationToken(internalConfiguration.authenticationToken())
            .authenticationUser(internalConfiguration.authenticationUser())
            .authenticationPassword(internalConfiguration.authenticationPassword())

        BackendFactory.instance(
            internalConfiguration.backend(),
            internalConfiguration.parameters(),
            backendConfiguration,
            internalConfiguration.debug()
        )
    }

    fun search(
        querySpecification: QuerySpecification,
        dateRange: QueryDateRange,
        progressHandler: ProgressHandler?,
        numberOfResults: Long?,
        resultHandler: ResultHandler
    ) {
        progressHandler?.start(backend.javaClass.simpleName)
        try {
            backend.search(
                { result, _ ->
                    progressHandler?.add(result.count(), result.total())
                    resultHandler(result)
                },
                querySpecification,
                dateRange.truncate(ChronoUnit.MILLIS),
                numberOfResults,
                false
            )
            progressHandler?.done()
        } catch (e: Exception) {
            progressHandler?.error(e)
            throw e
        }
    }

    fun newQuery() = QuerySpecification.empty().withQuerySet(internalConfiguration.query().flatten())
        .withNotQuerySet(internalConfiguration.notQuery().flatten())!!

    override fun close() = backend.close()
}

class RangeSearch(val environment: Environment, from: ZonedDateTime, to: ZonedDateTime) {
    init {
        require(from.nano == 0)
        require(to.nano == 0)
        require(from.isBefore(to))
    }

    val range = QueryDateRange(from, to)

    private val query = environment.newQuery()

    var empty = true
        private set

    fun search(resultHandler: ResultHandler) {
        environment.search(query, range, null, null) { result ->
            if (result.count() > 0) {
                empty = false
                resultHandler(result)
            }
        }
    }

}

class LogExplorerRepository(val environment: Environment) : LogRepository<Nothing, LogEntry<Nothing>> {
    override val configuration = environment.configuration

    override fun query(
        logQuery: LogRepository.LogQuery,
        entryHandler: (LogEntry<Nothing>) -> Unit
    ): LogRepository.LogQueryResponse {
        val rangeSearch = RangeSearch(environment, logQuery.from, logQuery.to)
        rangeSearch.search { result ->
            result.results().forEach {
                with(AttributesAdapter(it)) {
                    if (logQuery.criteria?.match(this) != false)
                        entryHandler(this)
                }
            }
        }

        return LogRepository.LogQueryResponse(logQuery, rangeSearch.empty)
    }

    private class AttributesAdapter(val result: SearchResult.Attributes) : LogEntry<Nothing> {
        override val timestamp = result.timestamp

        override val keys: Set<String>
            get() {
                val keys = HashSet<String>()

                fun rec(prefix: String, map: Map<*, *>) {
                    map.forEach { (key, value) ->
                        if (key is String) {
                            keys.add(prefix + key)
                            if (value is Map<*, *>) {
                                rec("$prefix$key.", value)
                            }
                        }
                    }
                }

                rec("", result)

                return keys
            }

        override operator fun get(key: String): String? {

            fun rec(key: String, map: Map<*, *>): Any? =
                if (map[key] != null) {
                    map[key]
                } else if ('.' in key) {
                    val firstKey = key.substring(0, key.indexOf('.'))
                    val value = map[firstKey] as? Map<*, *>
                    if (value != null) {
                        rec(key.substring(key.indexOf('.') + 1), value)
                    } else {
                        null
                    }
                } else {
                    null
                }

            return rec(key, result)?.toString()
        }

        override val uid: Nothing
            get() = throw UnsupportedOperationException()
        override val raw by lazy { result.toString() }
    }

    override fun get(uid: Nothing) = throw UnsupportedOperationException()

    override fun close() = environment.close()

    override val entryKeyType = Nothing::class
}
