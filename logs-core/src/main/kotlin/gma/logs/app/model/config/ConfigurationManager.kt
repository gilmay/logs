package gma.logs.app.model.config

import gma.commons.beans.UUIDPersistenceDelegate
import gma.commons.fx.preferences
import gma.commons.kt.asUnmodifiable
import gma.logs.app.model.config.spi.ConfigurationProvider
import org.xml.sax.InputSource
import java.beans.XMLDecoder
import java.beans.XMLEncoder
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object ConfigurationManager {

    private val providersByCategory = ServiceLoader.load(ConfigurationProvider::class.java)
        .asSequence().flatMap { provider -> provider.categories().map { it to provider } }
        .toMap(TreeMap(compareBy(ConfigurationCategory::name)))

    val categories = providersByCategory.keys.asUnmodifiable()

    private val configurationsByCategory = HashMap<ConfigurationCategory, List<LogsConfiguration>>()

    private val ConfigurationCategory.preferences
        get() = preferences().node("configuration").node(uuid.toString())

    init {
        providersByCategory.forEach { (category, provider) ->

            val configurations = ArrayList<LogsConfiguration>()

            with(category.preferences) {
                keys().forEach {
                    val configuration = get(it, null)?.let { string ->
                        XMLDecoder(InputSource(StringReader(string))).use { input ->
                            provider.load(category, input)
                        }
                    }
                    if (configuration != null) configurations += configuration
                }
            }

            configurationsByCategory[category] = configurations.asUnmodifiable()
        }
    }

    private fun save(category: ConfigurationCategory) {
        // Put new/existing configurations:
        configurationsByCategory[category]?.forEach { config ->
            val bytes = ByteArrayOutputStream()

            XMLEncoder(bytes).use { output ->
                UUIDPersistenceDelegate.register(output)
                providersByCategory[category]!!.save(config, output)
            }

            with(category.preferences) {
                put(config.uuid.toString(), bytes.toString(StandardCharsets.UTF_8))
                flush()
            }
        }

        // Remove deleted configurations:
        with(category.preferences) {
            keys().filter { key ->
                configurationsByCategory[category]?.find { it.uuid.toString() == key } == null
            }.forEach { remove(it) }
        }
    }

    fun getConfigurations(category: ConfigurationCategory): List<LogsConfiguration> =
        configurationsByCategory[category]!!

    fun newConfiguration(category: ConfigurationCategory) =
        providersByCategory[category]!!.newConfiguration(category).apply {
            if (name.isBlank()) name = "${category.name}*"
        }

    fun duplicateConfiguration(configuration: LogsConfiguration) = configuration.copy().apply {
        if (name.isBlank() || name == configuration.name) name = "${configuration.name}*"
    }

    fun replaceConfigurations(category: ConfigurationCategory, configurations: List<LogsConfiguration>) {
        val copy = ArrayList(configurations) // may be encoded by XmlEncoder
        require(copy.all { it.category == category })
        configurationsByCategory[category] = copy
        save(category)
    }

    fun getEditorFragmentClass(category: ConfigurationCategory) =
        providersByCategory[category]!!.editorFragmentClass(category)

    fun getConfiguration(uuid: UUID): LogsConfiguration {
        configurationsByCategory.values.forEach { list ->
            list.find { it.uuid == uuid }?.let { return@getConfiguration it }
        }
        throw IllegalArgumentException(uuid.toString())
    }

}
