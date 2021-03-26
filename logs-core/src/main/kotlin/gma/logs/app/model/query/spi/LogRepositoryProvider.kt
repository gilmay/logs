package gma.logs.app.model.query.spi

import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.query.LogEntry
import gma.logs.app.model.query.LogRepository
import java.util.ServiceLoader

abstract class LogRepositoryProvider {

    companion object {
        fun getLogRepository(configuration: LogsConfiguration): LogRepository<Any, LogEntry<Any>> {
            for (provider in ServiceLoader.load(LogRepositoryProvider::class.java)) {
                val repo = provider.getLogRepository(configuration)
                if (repo != null) {
                    return repo
                }
            }
            throw IllegalStateException()
        }
    }

    abstract fun getLogRepository(configuration: LogsConfiguration): LogRepository<Any, LogEntry<Any>>?
}
