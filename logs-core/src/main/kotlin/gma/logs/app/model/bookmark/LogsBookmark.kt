package gma.logs.app.model.bookmark

import gma.logs.app.controller.logsinstance.ResultsController
import gma.logs.app.model.config.LogsConfiguration
import java.time.LocalDateTime

class LogsBookmark(
    val name: String,
    val configuration: LogsConfiguration,
    val from: LocalDateTime?, val to: LocalDateTime?,
    val criteriaString: String?,
    val visibleKeys: List<String>,
    val timestampFormat: ResultsController.TimestampFormat,
    val isMultiline: Boolean
) {
    init {
        require(name.isNotBlank())
    }
}
