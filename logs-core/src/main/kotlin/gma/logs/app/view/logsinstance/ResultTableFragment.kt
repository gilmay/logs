package gma.logs.app.view.logsinstance

import gma.logs.app.controller.logsinstance.LogsTabViewModel
import gma.logs.app.controller.logsinstance.ResultsController
import gma.logs.app.model.query.CriteriaParser
import gma.logs.app.model.query.LogEntry
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.util.Callback
import tornadofx.*
import java.text.NumberFormat

class ResultTableFragment : Fragment() {

    private var TableColumn<LogEntry<*>, *>.key: ResultsController.Key?
        get() = properties["key"] as ResultsController.Key?
        set(value) {
            properties["key"] = value
        }

    private val controller by inject<ResultsController>()

    override val root = stackpane {

        stackpane {
            visibleWhen { controller.loadingProperty }
            progressbar()
            label("Loading...") {
                useMaxSize = true
                alignment = Pos.CENTER
            }
        }

        borderpane {
            hiddenWhen { controller.loadingProperty }

            top = label(controller.hitCountProperty.stringBinding {
                val nf = NumberFormat.getIntegerInstance()
                it?.let { if (it > 1) "${nf.format(it)} hits" else "${nf.format(it)} hit" }
            })

            center = tableview(controller.resultsProperty) {

                // === Listen to properties impacting display ===
                controller.multilineProperty.onChange { refresh() }
                controller.timestampFormatProperty.onChange { refresh() }

                // === Static columns ===
                readonlyColumn("Timestamp", LogEntry<*>::timestamp) {
                    cellFormat {
                        val float = find<FloatingHoverPane>()
                        float.text = controller.timestampFormatProperty.value.formatter.format(item)
                        float.excludeVisible = false
                        float.includeVisible = false
                        graphic = float.root
                    }
                }

                // === Dynamic columns ===
                controller.knownKeys.forEach { handleNewKey(it) }
                controller.knownKeys.addListener(ListChangeListener {
                    while (it.next()) {
                        if (it.wasAdded()) {
                            it.addedSubList.forEach { key ->
                                handleNewKey(key)
                            }
                        }
                    }
                })

            }
        }
    }

    private fun TableView<LogEntry<*>>.handleNewKey(key: ResultsController.Key) {
        if (key.isVisible) showColumn(key)
        key.visibleProperty.onChange { visible ->
            if (visible) showColumn(key)
            else hideColumn(key)
        }
    }

    private fun TableView<LogEntry<*>>.hideColumn(key: ResultsController.Key?) {
        columns.removeIf { col -> col.key == key }
    }

    private fun TableView<LogEntry<*>>.showColumn(key: ResultsController.Key) {
        column(key.name, String::class) {
            this.key = key
            cellFormat {
                val float = find<FloatingHoverPane>()
                float.text = item.toString(controller.multilineProperty.value)
                if (key.isMeta) {
                    float.includeVisible = false
                    float.excludeVisible = false
                } else {
                    float.onIncludeAction = EventHandler { appendInclude(key.name, float.text) }
                    float.onExcludeAction = EventHandler { appendExclude(key.name, float.text) }
                }
                graphic = float.root
            }
            cellValueFactory = Callback { cell ->
                ReadOnlyStringWrapper(key[cell.value])
            }
        }
    }

    private fun Any?.toString(multiline: Boolean?): String? {
        if (this == null) return null
        if (multiline == true) {
            return this.toString()
        }
        return this.toString().replace(Regex("\\R"), "↩")
    }

    private fun appendInclude(key: String, value: String?) {
        val safeKey = CriteriaParser.escapeIfRequired(key)
        val expression =
            if (value == null) "$safeKey is empty"
            else "$safeKey === ${CriteriaParser.escapeIfRequired(value)}"
        append(expression)
    }

    private fun appendExclude(key: String, value: String?) {
        val safeKey = CriteriaParser.escapeIfRequired(key)
        val expression =
            if (value == null) {
                "$safeKey is not empty"
            } else {
                "$safeKey !== ${CriteriaParser.escapeIfRequired(value)}"
            }
        append(expression)
    }

    private fun append(expression: String) {
        with(find<LogsTabViewModel>().criteriaStringProperty) {
            if (get().isNullOrBlank()) {
                set(expression)
            } else {
                val existing = get().trimEnd()

                if (existing.endsWith("or") || existing.endsWith("||")
                    || existing.endsWith("and") || existing.endsWith("&&")
                ) {
                    set("$existing $expression")
                } else {
                    set("$existing and $expression")
                }
            }
        }
    }

}
