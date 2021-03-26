package gma.logs.app.view.logsinstance

import gma.logs.app.controller.logsinstance.ResultsController
import tornadofx.*
import java.time.ZonedDateTime

class OptionsFragment : Fragment() {

    private val controller by inject<ResultsController>()

    private val exampleDate = ZonedDateTime.now()

    override val root = form {
        fieldset("Text") {
            field {
                checkbox("Multiline", controller.multilineProperty)
            }
        }
        fieldset("Timestamp") {
            field("Format") {
                combobox(controller.timestampFormatProperty, ResultsController.TimestampFormat.values().toList()) {
                    cellFormat {
                        this.text = item.formatter.format(exampleDate)
                    }
                }
            }
        }
    }
}
