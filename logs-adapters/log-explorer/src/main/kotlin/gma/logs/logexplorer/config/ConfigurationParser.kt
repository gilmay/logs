package gma.logs.logexplorer.config

import com.tsquare.logexplorer.config.YamlConfiguration
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

internal object ConfigurationParser {

    fun listProfiles(configFile: String?) = configFile?.let { listProfiles(File(configFile).toURI()) } ?: emptySet()

    private fun listProfiles(configFile: URI): Set<String> {
        try {
            val c = configFile.toURL().openConnection()
            c.getInputStream().use {
                return Yaml().loadAs(it, YamlConfiguration.ConfigurationModel::class.java).profiles.keys.toSortedSet()
            }
        } catch (ex: Exception) {
            logger.warn(ex) { "Cannot parse configuration at $configFile" }
            return emptySet()
        }
    }

    fun validateConfigFile(configFile: String?) = configFile?.let { validateConfigFile(File(it).toURI()) }

    private fun validateConfigFile(configFile: URI): String? =
        try {
            val c = configFile.toURL().openConnection()
            c.getInputStream().use {
                Yaml().loadAs(it, YamlConfiguration.ConfigurationModel::class.java)
            }
            null
        } catch (ex: Exception) {
            ex.localizedMessage
        }

}
