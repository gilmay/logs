package gma.logs.logexplorer.query

import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.query.LogEntry
import gma.logs.app.model.query.LogRepository
import gma.logs.app.model.query.spi.LogRepositoryProvider
import gma.logs.logexplorer.config.LogExplorerConfiguration
import gma.logs.logexplorer.search.Environment
import gma.logs.logexplorer.search.LogExplorerRepository

class LogExplorerLogRepositoryProvider : LogRepositoryProvider() {

    override fun getLogRepository(configuration: LogsConfiguration): LogRepository<Any, LogEntry<Any>>? =
        (configuration as? LogExplorerConfiguration)?.let {
            LogExplorerRepository(Environment(it)) as LogRepository<Any, LogEntry<Any>>?
        }

}
