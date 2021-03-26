package gma.logs.app.controller.config

import gma.logs.app.model.config.ConfigurationManager
import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.config.spi.BindingFactory
import javafx.beans.property.StringProperty
import tornadofx.*

class ConfigurationScope(val configuration: LogsConfiguration, val controller: ConfigurationsEditController) : Scope(),
    BindingFactory {

    val itemViewModel = find<ItemViewModel<LogsConfiguration>>(this)

    val nameBinding = bind(configuration.nameProperty)

    val fragmentLazy = lazy {
        find(
            ConfigurationManager.getEditorFragmentClass(configuration.category), this,
            "configuration" to configuration,
            "bindingFactory" to this
        )
    }
    val fragment by fragmentLazy

    override fun bind(property: StringProperty): StringProperty = itemViewModel.bind { property }

}
