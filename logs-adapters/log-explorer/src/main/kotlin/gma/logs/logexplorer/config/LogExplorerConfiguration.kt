package gma.logs.logexplorer.config

import gma.commons.kt.toUnmodifiable
import gma.logs.app.model.config.LogsConfiguration
import tornadofx.*
import java.util.UUID

class LogExplorerConfiguration(uuid: UUID) : LogsConfiguration(uuid, logExplorerCategory) {

    companion object {
        fun splitProfiles(string: String?) = string?.split(",", ";")?.map { it.trim() }?.toUnmodifiable() ?: emptyList()
    }

    override val nameProperty = stringProperty("")
    override var name by nameProperty

    val configurationFileProperty = stringProperty("")
    var configurationFile by configurationFileProperty

    val profilesProperty = stringProperty("")
    var profiles by profilesProperty

    override fun copy() = LogExplorerConfiguration(UUID.randomUUID()).also {
        it.configurationFile = configurationFile
        it.profiles = profiles
    }

    val profileList
        get() = splitProfiles(profiles)

}
