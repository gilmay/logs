package gma.logs.app.model.query

import gma.logs.app.model.config.LogsConfiguration
import java.io.Closeable
import java.time.ZonedDateTime
import kotlin.reflect.KClass

interface LogRepository<U : Any, E : LogEntry<U>> : Closeable {
    class LogQuery(val from: ZonedDateTime, val to: ZonedDateTime, val criteria: Criteria?) {
        init {
            require(from.nano == 0)
            require(to.nano == 0)
            require(from.isBefore(to))
        }
    }

    class LogQueryResponse(val query: LogQuery, val isEmpty: Boolean)

    val configuration: LogsConfiguration

    fun query(logQuery: LogQuery, entryHandler: (E) -> Unit): LogQueryResponse

    operator fun get(uid: U): LogEntry<U>?

    val entryKeyType: KClass<U>
}
