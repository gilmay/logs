package gma.logs.logexplorer.config

import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.config.spi.ConfigurationProvider
import gma.logs.logexplorer.config.fx.LogExplorerConfigurationFragment
import java.beans.DefaultPersistenceDelegate
import java.beans.XMLEncoder
import java.util.UUID

internal val logExplorerCategory =
    ConfigurationCategory(UUID.fromString("cb1b525b-894a-495b-9138-7b8a93f83055"), "Log Explorer")

class LogExplorerConfigurationProvider : ConfigurationProvider() {
    override fun categories() = listOf(logExplorerCategory)

    override fun newConfiguration(category: ConfigurationCategory) =
        LogExplorerConfiguration(UUID.randomUUID())

    override fun save(configuration: LogsConfiguration, encoder: XMLEncoder) {
        encoder.setPersistenceDelegate(LogExplorerConfiguration::class.java, persistenceDelegate)
        encoder.writeObject(configuration)
    }

    override fun editorFragmentClass(category: ConfigurationCategory) =
        LogExplorerConfigurationFragment::class
}

private val persistenceDelegate = DefaultPersistenceDelegate(arrayOf("uuid"))
