package gma.logs.app.controller.logsinstance

import tornadofx.*
import java.time.Duration
import java.time.LocalDateTime

class LogsTabViewModel : ViewModel() {
    override val scope = super.scope as LogsScope

    val fromProperty = bind { scope.fromProperty }.apply { onChange { validationContext.validate() } }
    val toProperty = bind { scope.toProperty }.apply { onChange { validationContext.validate() } }

    val criteriaStringProperty =
        bind { scope.criteriaStringProperty }.apply { onChange { validationContext.validate() } }

    fun refresh() = scope.refresh()

    init {
        // Empty Model is initially dirty
        runLater {
            if (toProperty.value == null || fromProperty.value == null) {
                toProperty.value = LocalDateTime.now().withNano(0)
                fromProperty.value = toProperty.value.minus(Duration.ofMinutes(15))
            }
        }
    }

}
