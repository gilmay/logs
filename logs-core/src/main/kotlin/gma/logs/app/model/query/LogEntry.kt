package gma.logs.app.model.query

import java.time.ZonedDateTime

interface LogEntry<U> {
    val uid: U
    val raw: String
    val timestamp: ZonedDateTime
    val keys: Set<String>
    operator fun get(key: String): String?
}
