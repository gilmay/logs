package gma.logs.app.model.config.spi

import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.LogsConfiguration
import tornadofx.*
import java.beans.XMLDecoder
import java.beans.XMLEncoder
import kotlin.reflect.KClass

abstract class ConfigurationProvider {

    abstract fun categories(): Collection<ConfigurationCategory>

    open fun save(configuration: LogsConfiguration, encoder: XMLEncoder) {
        encoder.writeObject(configuration)
    }

    open fun load(category: ConfigurationCategory, decoder: XMLDecoder): LogsConfiguration? {
        return try {
            decoder.readObject() as LogsConfiguration
        } catch (ex: Exception) {
            null
        }
    }

    abstract fun newConfiguration(category: ConfigurationCategory): LogsConfiguration

    abstract fun editorFragmentClass(category: ConfigurationCategory): KClass<out Fragment>

}
