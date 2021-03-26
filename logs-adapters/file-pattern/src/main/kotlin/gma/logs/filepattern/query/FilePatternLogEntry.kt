package gma.logs.filepattern.query

import gma.logs.app.model.query.LogEntry
import java.time.ZonedDateTime

class FilePatternLogEntry(
    override val timestamp: ZonedDateTime,
    override val keys: Set<String>,
    override val raw: String,
    private val map: Map<String, String>
) : LogEntry<Nothing> {

    override val uid: Nothing
        get() = throw UnsupportedOperationException()

    override fun get(key: String) = map[key]
}
