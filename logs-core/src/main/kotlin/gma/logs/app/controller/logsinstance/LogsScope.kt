package gma.logs.app.controller.logsinstance

import gma.logs.app.model.bookmark.LogsBookmark
import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.query.CriteriaParser
import gma.logs.app.model.query.LogRepository
import gma.logs.app.model.query.spi.LogRepositoryProvider
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class LogsScope(val configuration: LogsConfiguration) : Scope() {

    companion object {
        fun fromBookmark(bookmark: LogsBookmark) =
            LogsScope(bookmark.configuration).apply {
                bookmarkName = bookmark.name
                from = bookmark.from
                to = bookmark.to
                criteriaString = bookmark.criteriaString

                with(find<ResultsController>(this)) {
                    visibleKeysNames = bookmark.visibleKeys
                    timestampFormat = bookmark.timestampFormat
                    isMultiline = bookmark.isMultiline
                }
            }
    }

    val logRepository = LogRepositoryProvider.getLogRepository(configuration)

    val bookmarkNameProperty = SimpleStringProperty()
    var bookmarkName: String? by bookmarkNameProperty

    val toProperty = SimpleObjectProperty<LocalDateTime>()
    var to: LocalDateTime? by toProperty

    val fromProperty = SimpleObjectProperty<LocalDateTime>()
    var from: LocalDateTime? by fromProperty

    val criteriaStringProperty = SimpleStringProperty()
    var criteriaString by criteriaStringProperty

    val criteriaBinding = criteriaStringProperty.objectBinding {
        try {
            it?.let { CriteriaParser(it).parse() }
        } catch (e: ParseException) {
            null
        }
    }
    val criteria by criteriaBinding

    private val refreshToggleProperty = SimpleBooleanProperty()
    fun refresh() {
        refreshToggleProperty.value = !refreshToggleProperty.value
    }

    val queryBinding = criteriaBinding.objectBinding(fromProperty, toProperty, refreshToggleProperty) {
        if (from == null || to == null) {
            null
        } else {
            LogRepository.LogQuery(
                ZonedDateTime.of(from, ZoneId.systemDefault()),
                ZonedDateTime.of(to, ZoneId.systemDefault()),
                criteria
            )
        }
    }

    fun toBookmark(name: String) =
        find<ResultsController>(this).let { resultsController ->
            LogsBookmark(
                name,
                configuration, from, to,
                criteriaString,
                resultsController.visibleKeysNames,
                resultsController.timestampFormat,
                resultsController.isMultiline
            )
        }

    fun dispose() = logRepository.close()

}
