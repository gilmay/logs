package gma.logs.app.controller.config

import gma.commons.kt.asUnmodifiable
import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.ConfigurationManager
import gma.logs.app.model.config.LogsConfiguration
import tornadofx.*

class ConfigurationsEditViewModel(private val controller: ConfigurationsEditController) : Controller() {

    private val scopesByCategory = ConfigurationManager.categories.map { category ->
        category to ConfigurationManager.getConfigurations(category).map { ConfigurationScope(it, controller) }
            .toObservable()
    }.toMap().asUnmodifiable()

    fun addConfiguration(configuration: LogsConfiguration): ConfigurationScope {
        val scope = ConfigurationScope(configuration, controller)
        scopesByCategory[configuration.category]!!.add(scope)
        return scope
    }

    fun removeConfiguration(scope: ConfigurationScope) {
        scopesByCategory[scope.configuration.category]!!.remove(scope)
        scope.deregister()
    }

    fun getConfigurations(configurationCategory: ConfigurationCategory) =
        scopesByCategory[configurationCategory]!!

    private val activatedScopes get() = scopesByCategory.values.flatten().filter { it.fragmentLazy.isInitialized() }

    val validators get() = activatedScopes.flatMap { it.itemViewModel.validationContext.validators }

    fun commit(): Boolean {
        if (scopesByCategory.values.flatten().isEmpty()) {
            scopesByCategory.keys.forEach { ConfigurationManager.replaceConfigurations(it, emptyList()) }
            fireConfigurationsUpdated()
            return true
        }

        val valid =
            activatedScopes.isEmpty() || activatedScopes.all { it.itemViewModel.validate(focusFirstError = false) }
        if (!valid) {
            return false
        }
        activatedScopes.withEach { itemViewModel.commit() }

        scopesByCategory.withEach { ConfigurationManager.replaceConfigurations(key, value.map { it.configuration }) }
        fireConfigurationsUpdated()
        return true
    }

    private fun fireConfigurationsUpdated() = fire(ConfigurationsChanged)

}
