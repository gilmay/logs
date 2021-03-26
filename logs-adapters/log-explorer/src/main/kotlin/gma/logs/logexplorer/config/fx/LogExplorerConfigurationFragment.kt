package gma.logs.logexplorer.config.fx

import gma.logs.app.model.config.spi.BindingFactory
import gma.logs.logexplorer.config.ConfigurationParser
import gma.logs.logexplorer.config.LogExplorerConfiguration
import tornadofx.*

class LogExplorerConfigurationFragment : Fragment() {

    private val configuration by param<LogExplorerConfiguration>()
    private val bindingFactory by param<BindingFactory>()

    inner class Bindings {
        val configurationDirectoryProperty = bindingFactory.bind(configuration.configurationFileProperty)
        val profilesProperty = bindingFactory.bind(configuration.profilesProperty)
    }

    private val bindings = Bindings()

    override val root = form {
        fieldset {
            field("Log Explorer Configuration file") {
                textfield(bindings.configurationDirectoryProperty) {
                    required()
                    validator {
                        ConfigurationParser.validateConfigFile(it)?.let(this::warning)
                    }
                }
            }
            field("Log Explorer Profiles") {
                textfield(bindings.profilesProperty) {
                    required()
                    validator { profilesString ->
                        val profiles = ConfigurationParser.listProfiles(bindings.configurationDirectoryProperty.value)
                        val missingProfiles =
                            LogExplorerConfiguration.splitProfiles(profilesString).filter { it !in profiles }
                                .joinToString()
                        if (missingProfiles.isEmpty()) null
                        else warning("Unknown profiles: $missingProfiles")
                    }
                }
            }
        }
    }
}
