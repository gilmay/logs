package gma.logs.app.view.logsinstance

import gma.commons.kt.collapseBlanks
import gma.logs.app.controller.logsinstance.LogsScope
import gma.logs.app.controller.logsinstance.LogsTabViewModel
import gma.logs.app.model.query.CriteriaParser
import gma.logs.app.view.AppStyle
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.util.StringConverter
import tornadofx.*
import java.text.ParseException
import java.time.LocalDateTime
import java.time.Year
import java.time.format.*
import java.time.temporal.ChronoField

class LogsTabFragment : Fragment() {

    override val scope = super.scope as LogsScope

    private val viewModel by inject<LogsTabViewModel>()

    init {
        titleProperty.bind(
            scope.bookmarkNameProperty.stringBinding(scope.configuration.nameProperty) {
                scope.bookmarkName ?: scope.configuration.name
            }
        )
    }

    override val root = borderpane {
        top = hbox {
            val childrenInsets = Insets(1.0)
            textarea(viewModel.criteriaStringProperty) {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                    margin = childrenInsets
                }

                prefRowCount = 1

                onKeyPressed = EventHandler {
                    if (it.isControlDown && it.code == KeyCode.ENTER) viewModel.commit()
                }

                validator(this, viewModel.criteriaStringProperty, ValidationTrigger.OnChange(800)) {
                    return@validator try {
                        it?.let { CriteriaParser(it).parse() }
                        null
                    } catch (e: ParseException) {
                        error(e.localizedMessage)
                    }
                }
            }
            button("?").action {
                alert(
                    Alert.AlertType.INFORMATION,
                    "Query Syntax",
                    "Example:\n" +
                            "message is not empty or \"special field\" == 3\n" +
                            "\n" +
                            "Field/Value operators:\n" +
                            "compare:  ==  !=  <  <=  >=  >\n" +
                            "compare with case:  ===  !==\n" +
                            "regex match:  ~=  !~=  ~==  !~==\n" +
                            "is empty    is not empty\n" +
                            "\n" +
                            "Logical operators:\n" +
                            "&&  and  ||  or",
                    ButtonType.OK,
                )
            }

            textfield {
                hboxConstraints {
                    margin = childrenInsets
                }
                SafeLocalDateTimeFormat.bind(textProperty(), viewModel.fromProperty)

                validator(this, viewModel.fromProperty, ValidationTrigger.None) { from ->
                    when {
                        from == null -> error()
                        viewModel.toProperty.value?.let { to -> !from.isBefore(to) } == true -> {
                            error("To must be strictly after From")
                        }
                        else -> null
                    }
                }

                focusedProperty().onChange {
                    if (!it) {
                        text = SafeLocalDateTimeFormat.reformat(text)
                    }
                }
            }

            textfield {
                hboxConstraints {
                    margin = childrenInsets
                }
                SafeLocalDateTimeFormat.bind(textProperty(), viewModel.toProperty)

                validator(this, viewModel.toProperty, ValidationTrigger.None) { to ->
                    when {
                        to == null -> error()
                        viewModel.fromProperty.get()?.let { from -> !from.isBefore(to) } == true -> {
                            error("To must be strictly after From")
                        }
                        else -> null
                    }
                }

                focusedProperty().onChange {
                    if (!it) {
                        text = SafeLocalDateTimeFormat.reformat(text)
                    }
                }
            }

            button("Apply") {
                hboxConstraints {
                    margin = childrenInsets
                }

                setMinSize(USE_PREF_SIZE, USE_PREF_SIZE)
                enableWhen(viewModel::dirty)
                isDefaultButton = true
                action(viewModel::commit)
            }

            button("▼") {
                hboxConstraints {
                    margin = childrenInsets
                }
                addClass(AppStyle.dropdownLikeButton)

                val menu = ContextMenu()
                menu.items += MenuItem("Refresh").apply {
                    action { viewModel.refresh() }
                }
                action {
                    val bounds = localToScreen(boundsInLocal)
                    menu.show(this, bounds.minX, bounds.maxY)
                }

            }

        }

        left = drawer {
            item("Columns") {
                add(find<FieldsListFragment>().root)
            }
            item("Options") {
                add(find<OptionsFragment>().root)
            }
        }

        center = find<ResultTableFragment>().root
    }
}

internal object SafeLocalDateTimeFormat : StringConverter<LocalDateTime>() {
    fun bind(stringProperty: StringProperty, property: ObjectProperty<LocalDateTime>) = bindStringProperty(
        stringProperty,
        this,
        null,
        property,
        false
    )

    fun reformat(string: String?) = fromString(string)?.let { toString(it) } ?: string

    private val mainFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .toFormatter()

    private val secondaryFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatterBuilder()
            .appendValueReduced(ChronoField.YEAR, 2, 4, Year.now().value - 80)
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter()
    )

    override fun toString(obj: LocalDateTime?) =
        obj?.let {
            mainFormatter.format(obj)
        }

    override fun fromString(string: String?) =
        string?.collapseBlanks()?.trim()?.replace('/', '-')?.let { cleaned ->
            try {
                return@let mainFormatter.parse(cleaned, LocalDateTime::from)
            } catch (ex: DateTimeParseException) {
                secondaryFormatters.forEach {
                    try {
                        return@let it.parse(cleaned, LocalDateTime::from)
                    } catch (ex: DateTimeParseException) {
                    }
                }
            }
            return@let null
        }
}
