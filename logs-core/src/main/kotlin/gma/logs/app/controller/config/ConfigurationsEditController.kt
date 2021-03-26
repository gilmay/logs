package gma.logs.app.controller.config

import gma.commons.fx.findExpandAndSelect
import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.ConfigurationManager
import gma.logs.app.view.config.ConfigurationEditCommonTreeFragment
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import tornadofx.*

class ConfigurationsEditController : Controller() {

    val selectedItemProperty = SimpleObjectProperty<Any>()

    val viewModel = ConfigurationsEditViewModel(this)

    val treeFormatter: TreeCell<*>.(Any) -> Unit = {
        if (it is ConfigurationScope) {
            textProperty().bind(it.nameBinding)
        } else {
            text = (it as ConfigurationCategory).name
        }
    }

    fun treeChildFactory(item: TreeItem<Any>): Iterable<Any>? = with(item.value) {
        when (this) {
            is ConfigurationCategory -> viewModel.getConfigurations(this)
            is ConfigurationScope -> null
            else /* root */ -> ConfigurationManager.categories
        }
    }

    fun okAction(): Boolean {
        if (!viewModel.commit()) {
            val failed = viewModel.validators.first { !it.validate(true) }
            failed.node.findParent<ConfigurationEditCommonTreeFragment>()?.let {
                selectedItemProperty.value = it.scope
            }
            failed.node.requestFocus()
            return false
        }
        return true
    }

    val canAddNewConfigurationBinding = selectedItemProperty.booleanBinding { it != null }
    fun onAddNewConfiguration(tree: TreeView<Any>) {
        val selected = selectedItemProperty.value
        val category =
            if (selected is ConfigurationScope) selected.configuration.category else selected as ConfigurationCategory

        val configuration = viewModel.addConfiguration(ConfigurationManager.newConfiguration(category))
        tree.findExpandAndSelect(configuration)
    }

    val canDeleteSelectedConfigurationBinding =
        selectedItemProperty.booleanBinding { it is ConfigurationScope }

    fun onDeleteSelectedConfiguration() {
        (selectedItemProperty.value as? ConfigurationScope)?.let {
            viewModel.removeConfiguration(it)
        }
    }

    val canDuplicateSelectedConfigurationBinding =
        selectedItemProperty.booleanBinding { it is ConfigurationScope }

    fun onDuplicateSelectedConfiguration(tree: TreeView<Any>) {
        (selectedItemProperty.value as? ConfigurationScope)?.let {
            val duplicate = viewModel.addConfiguration(ConfigurationManager.duplicateConfiguration(it.configuration))
            tree.findExpandAndSelect(duplicate)
        }
    }

}
