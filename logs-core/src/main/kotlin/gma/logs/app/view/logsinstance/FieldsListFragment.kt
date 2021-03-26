package gma.logs.app.view.logsinstance

import gma.logs.app.controller.logsinstance.ResultsController
import gma.logs.app.view.AppStyle
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.CheckBoxTableCell
import tornadofx.*

class FieldsListFragment : Fragment() {

    private val controller by inject<ResultsController>()

    override val root = tableview(controller.knownKeys) {
        isEditable = true

        itemsProperty().value.addListener(object : ListChangeListener<ResultsController.Key> {
            override fun onChanged(c: ListChangeListener.Change<out ResultsController.Key>?) {
                // Fixes the now showing initially bug:
                runLater { parent.layout() }
                itemsProperty().value.removeListener(this)
            }
        })

        readonlyColumn("", ResultsController.Key::name).remainingWidth()
        with(TableColumn<ResultsController.Key, ObservableValue<Boolean>>("\uD83D\uDC41")) {
            addClass(AppStyle.checkboxTableColumn)
            addColumnInternal(this)
            cellFactory = CheckBoxTableCell.forTableColumn {
                tableView.items[it].visibleProperty
            }
        }

        smartResize()
    }

}
