package gma.logs.app.view.logsinstance

import gma.logs.app.view.AppStyle
import javafx.beans.property.BooleanProperty
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.scene.control.Tooltip
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.Priority
import tornadofx.*

class FloatingHoverPane : Fragment() {

    var textProperty by singleAssign<StringProperty>()

    var onCopyActionProperty by singleAssign<Property<EventHandler<ActionEvent>>>()
    var onIncludeActionProperty by singleAssign<Property<EventHandler<ActionEvent>>>()
    var onExcludeActionProperty by singleAssign<Property<EventHandler<ActionEvent>>>()

    var copyVisibleProperty by singleAssign<BooleanProperty>()
    var includeVisibleProperty by singleAssign<BooleanProperty>()
    var excludeVisibleProperty by singleAssign<BooleanProperty>()

    override val root = gridpane {
        useMaxWidth = true

        label {
            this@FloatingHoverPane.textProperty = textProperty()
            gridpaneConstraints {
                columnRowIndex(0, 0)
                hGrow = Priority.ALWAYS
            }
        }

        gridpane {
            maxWidth = 0.0
            gridpaneConstraints {
                columnRowIndex(0, 0)
                hAlignment = HPos.RIGHT
            }
            row {
                button("✔") {
                    onIncludeActionProperty = onActionProperty()
                    includeVisibleProperty = visibleProperty()
                    tooltip = Tooltip("Include")
                    addClass(AppStyle.tinyHoverButton)
                }
                button("✘") {
                    onExcludeActionProperty = onActionProperty()
                    excludeVisibleProperty = visibleProperty()
                    tooltip = Tooltip("Exclude")
                    addClass(AppStyle.tinyHoverButton)
                }
                button("\uD83D\uDCCB") {
                    onCopyActionProperty = onActionProperty()
                    copyVisibleProperty = visibleProperty()
                    action {
                        clipboard.setContent(ClipboardContent().apply {
                            putString(this@FloatingHoverPane.text)
                        })
                    }
                    tooltip = Tooltip("Copy")
                    addClass(AppStyle.tinyHoverButton)
                }
            }
            visibleWhen { hoverProperty().or(parent.hoverProperty()) }
        }

    }

    var text by textProperty

    var onCopyAction by onCopyActionProperty
    var onIncludeAction by onIncludeActionProperty
    var onExcludeAction by onExcludeActionProperty

    var copyVisible by copyVisibleProperty
    var includeVisible by includeVisibleProperty
    var excludeVisible by excludeVisibleProperty
}
