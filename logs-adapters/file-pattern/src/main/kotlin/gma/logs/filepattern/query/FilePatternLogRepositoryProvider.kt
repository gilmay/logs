package gma.logs.filepattern.query

import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.query.LogEntry
import gma.logs.app.model.query.LogRepository
import gma.logs.app.model.query.spi.LogRepositoryProvider
import gma.logs.filepattern.config.FilePatternConfiguration

class FilePatternLogRepositoryProvider : LogRepositoryProvider() {

    override fun getLogRepository(configuration: LogsConfiguration): LogRepository<Any, LogEntry<Any>>? =
        (configuration as? FilePatternConfiguration)?.let(::FilePatternLogRepository) as LogRepository<Any, LogEntry<Any>>?

}
