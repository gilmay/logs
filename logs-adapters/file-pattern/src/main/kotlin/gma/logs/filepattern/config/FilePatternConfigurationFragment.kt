package gma.logs.filepattern.config

import gma.commons.kt.namedGroups
import gma.logs.app.model.config.spi.BindingFactory
import tornadofx.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter

class FilePatternConfigurationFragment : Fragment() {

    private val configuration by param<FilePatternConfiguration>()
    private val bindingFactory by param<BindingFactory>()

    inner class Bindings {
        val baseDirectory = bindingFactory.bind(configuration.baseDirProperty)
        val filePattern = bindingFactory.bind(configuration.filePatternProperty)
        val extractPattern = bindingFactory.bind(configuration.extractPatternProperty)
        val timestampFormat = bindingFactory.bind(configuration.timestampFormatProperty)
        val fileCharset = bindingFactory.bind(configuration.fileCharsetProperty)
    }

    private val bindings = Bindings()

    override val root = form {
        fieldset {
            field("Base directory") {
                textfield(bindings.baseDirectory) {
                    required()
                    validator {
                        try {
                            it?.let {
                                val p = Paths.get(it)
                                if (Files.isRegularFile(p)) {
                                    warning("\"$it\" is a regular file")
                                } else if (!Files.isReadable(p) || !Files.exists(p)) {
                                    warning("Cannot read \"$it\"")
                                } else {
                                    null
                                }
                            }
                        } catch (ex: IllegalArgumentException) {
                            error(ex.localizedMessage)
                        }
                    }
                }
            }
            field("Files pattern") {
                textfield(bindings.filePattern) {
                    required()
                    validator {
                        try {
                            it?.let(FilePatternConfiguration::filePatternToMatcher)
                            null
                        } catch (ex: IllegalArgumentException) {
                            error(ex.localizedMessage)
                        }
                    }
                }
            }
            field("Extractor regex") {
                textarea(bindings.extractPattern) {
                    isWrapText = true
                    required()
                    validator {
                        try {
                            it?.let {
                                val regex = Regex(it)
                                if ("timestamp" !in regex.namedGroups) {
                                    error("A group named \"timestamp\" is required")
                                } else {
                                    null
                                }
                            }
                        } catch (ex: IllegalArgumentException) {
                            error(ex.localizedMessage)
                        }
                    }
                }
            }
            field("Timestamp format") {
                textfield(bindings.timestampFormat) {
                    required()
                    validator {
                        try {
                            it?.let { DateTimeFormatter.ofPattern(it) }
                            null
                        } catch (ex: IllegalArgumentException) {
                            error(ex.localizedMessage)
                        }
                    }
                }
            }
            field("File charset") {
                combobox(bindings.fileCharset, Charset.availableCharsets().values.map(Charset::name)) {
                    required()
                }
            }
        }
    }
}
