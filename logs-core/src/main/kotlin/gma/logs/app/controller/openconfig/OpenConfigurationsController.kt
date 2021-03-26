package gma.logs.app.controller.openconfig

import gma.commons.fx.subscribeWithExceptionHandler
import gma.commons.kt.asUnmodifiable
import gma.commons.kt.toUnmodifiable
import gma.logs.app.controller.config.ConfigurationsChanged
import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.ConfigurationManager
import gma.logs.app.model.config.LogsConfiguration
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import tornadofx.*

class OpenConfigurationsController : Controller() {

    var configurationsByCategory: Map<ConfigurationCategory, List<LogsConfiguration>> by property(
        buildConfigurationsTree()
    )
    val configurationsByCategoryProperty = getProperty(OpenConfigurationsController::configurationsByCategory)

    init {
        subscribeWithExceptionHandler<ConfigurationsChanged> {
            configurationsByCategory = buildConfigurationsTree()
        }
    }

    private fun buildConfigurationsTree() = ConfigurationManager.categories.map { category ->
        category to ConfigurationManager.getConfigurations(category).toUnmodifiable()
    }.toMap().asUnmodifiable()

    val treeFormatter: TreeCell<*>.(Any) -> Unit = {
        text = when (it) {
            is LogsConfiguration -> it.name
            is ConfigurationCategory -> it.name
            else -> "?"
        }
    }

    fun treeChildFactory(item: TreeItem<Any>): Iterable<Any>? = with(item.value) {
        when (this) {
            is ConfigurationCategory -> configurationsByCategory[this]
            is LogsConfiguration -> null
            else /* root */ -> ConfigurationManager.categories
        }
    }

    val selectedItemProperty = SimpleObjectProperty<Any>()

    val canOpenBinding = selectedItemProperty.booleanBinding { it is LogsConfiguration }
    fun onOpen(): Boolean {
        fire(OpenConfigurationEvent(selectedItemProperty.value as LogsConfiguration))
        return true
    }
}
