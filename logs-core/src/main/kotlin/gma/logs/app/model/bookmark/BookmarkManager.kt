package gma.logs.app.model.bookmark

import gma.commons.fx.preferences
import gma.logs.app.controller.logsinstance.ResultsController
import gma.logs.app.model.config.ConfigurationManager
import mu.KotlinLogging
import org.xml.sax.InputSource
import tornadofx.*
import java.beans.XMLDecoder
import java.beans.XMLEncoder
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.collections.set

private val logger = KotlinLogging.logger {}

object BookmarkManager {

    private val preferences
        get() = preferences().node("bookmark")

    private val bookmarksByNameInternal = HashMap<String, LogsBookmark>().asObservable()

    val bookmarksByName = bookmarksByNameInternal.asUnmodifiable()

    init {
        with(preferences) {
            keys().forEach { key ->
                get(key, null)?.let { string ->
                    XMLDecoder(InputSource(StringReader(string))).use { input ->
                        try {
                            val bookmark = LogsBookmark(
                                key,
                                ConfigurationManager.getConfiguration(UUID.fromString(input.readObject() as String)),
                                (input.readObject() as? Long)?.let {
                                    LocalDateTime.ofEpochSecond(
                                        it,
                                        0,
                                        ZoneOffset.UTC
                                    )
                                },
                                (input.readObject() as? Long)?.let {
                                    LocalDateTime.ofEpochSecond(
                                        it,
                                        0,
                                        ZoneOffset.UTC
                                    )
                                },
                                input.readObject() as? String,
                                input.readObject() as? List<String> ?: emptyList(),
                                ResultsController.TimestampFormat.valueOf(input.readObject() as String),
                                input.readObject() as Boolean
                            )

                            bookmarksByNameInternal[bookmark.name] = bookmark
                        } catch (ex: Exception) {
                            logger.error(ex) { "Failure to load bookmark $key" }
                        }
                    }
                }
            }
        }

    }

    fun save(bookmark: LogsBookmark) {
        val bytes = ByteArrayOutputStream()
        XMLEncoder(bytes).use { output ->
            output.writeObject(bookmark.configuration.uuid.toString())
            output.writeObject(bookmark.from?.toEpochSecond(ZoneOffset.UTC))
            output.writeObject(bookmark.to?.toEpochSecond(ZoneOffset.UTC))
            output.writeObject(bookmark.criteriaString)
            output.writeObject(bookmark.visibleKeys)
            output.writeObject(bookmark.timestampFormat.name)
            output.writeObject(bookmark.isMultiline)
        }

        with(preferences) {
            put(bookmark.name, bytes.toString(StandardCharsets.UTF_8))
            flush()
        }

        bookmarksByNameInternal[bookmark.name] = bookmark
    }

    fun delete(bookmark: LogsBookmark) = delete(bookmark.name)

    fun delete(name: String) {
        with(preferences) {
            remove(name)
            flush()
        }
        bookmarksByNameInternal.remove(name)
    }

}
