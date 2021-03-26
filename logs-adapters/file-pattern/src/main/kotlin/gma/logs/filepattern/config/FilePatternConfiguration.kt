package gma.logs.filepattern.config

import gma.logs.app.model.config.ConfigurationCategory
import gma.logs.app.model.config.LogsConfiguration
import tornadofx.*
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.UUID

internal val filePatternCategory =
    ConfigurationCategory(UUID.fromString("5a33a945-4032-4f14-81b2-8db1206c8181"), "Files")

class FilePatternConfiguration(uuid: UUID) : LogsConfiguration(uuid, filePatternCategory) {

    companion object {
        fun filePatternToMatcher(pattern: String): PathMatcher {
            val syntaxAndPattern =
                if (pattern.startsWith("glob:") || pattern.startsWith("regex:"))
                    pattern
                else "glob:$pattern"
            return FileSystems.getDefault().getPathMatcher(syntaxAndPattern)
        }
    }

    override val nameProperty = stringProperty("")
    override var name by nameProperty

    val baseDirProperty = stringProperty()
    var baseDir by baseDirProperty

    val filePatternProperty = stringProperty()
    var filePattern by filePatternProperty

    val extractPatternProperty = stringProperty()
    var extractPattern by extractPatternProperty

    val timestampFormatProperty = stringProperty()
    var timestampFormat by timestampFormatProperty

    val fileCharsetProperty = stringProperty(StandardCharsets.UTF_8.name())
    var fileCharset by fileCharsetProperty

    override fun copy() = FilePatternConfiguration(UUID.randomUUID()).also {
        it.baseDir = baseDir
        it.filePattern = filePattern
        it.extractPattern = extractPattern
        it.timestampFormat = timestampFormat
        it.fileCharset = fileCharset
    }
}
