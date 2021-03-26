package gma.logs.app.view.system

import gma.logs.app.controller.system.MemoryController
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import tornadofx.*

class StatusBarFragment : Fragment() {

    private val memoryController by inject<MemoryController>()

    private lateinit var bar: ProgressBar
    private lateinit var text: Label

    override val root = hbox {
        region {
            hboxConstraints {
                hGrow = Priority.ALWAYS
            }
        }

        stackpane {
            hboxConstraints {
                margin = Insets(1.0)
            }
            bar = progressbar(memoryController.heapUsagePercentBinding)
            text = label(memoryController.heapUsageTextBinding)
        }

        tooltip().textProperty().bind(memoryController.memoryTextBinding)

        onLeftClick {
            memoryController.gc()
        }
    }
}
