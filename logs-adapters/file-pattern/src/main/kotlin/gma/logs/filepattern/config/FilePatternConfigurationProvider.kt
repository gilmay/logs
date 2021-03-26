package gma.logs.filepattern.config

import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.LogsConfiguration
import gma.logs.app.model.config.spi.ConfigurationProvider
import java.beans.DefaultPersistenceDelegate
import java.beans.XMLEncoder
import java.util.UUID

class FilePatternConfigurationProvider : ConfigurationProvider() {

    override fun categories(): Collection<ConfigurationCategory> = listOf(filePatternCategory)

    override fun newConfiguration(category: ConfigurationCategory) = FilePatternConfiguration(UUID.randomUUID())

    override fun save(configuration: LogsConfiguration, encoder: XMLEncoder) {
        encoder.setPersistenceDelegate(FilePatternConfiguration::class.java, persistenceDelegate)
        encoder.writeObject(configuration)
    }

    override fun editorFragmentClass(category: ConfigurationCategory) = FilePatternConfigurationFragment::class

}

private val persistenceDelegate = DefaultPersistenceDelegate(arrayOf("uuid"))
