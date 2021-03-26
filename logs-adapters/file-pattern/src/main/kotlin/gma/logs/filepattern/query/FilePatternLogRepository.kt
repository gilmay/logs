package gma.logs.filepattern.query

import gma.commons.kt.namedGroups
import gma.logs.app.model.query.LogRepository
import gma.logs.filepattern.config.FilePatternConfiguration
import mu.KotlinLogging
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

class FilePatternLogRepository(override val configuration: FilePatternConfiguration) :
    LogRepository<Nothing, FilePatternLogEntry> {

    override fun query(
        logQuery: LogRepository.LogQuery,
        entryHandler: (FilePatternLogEntry) -> Unit
    ): LogRepository.LogQueryResponse {

        val regex = Regex(configuration.extractPattern)
        val groups = regex.namedGroups.toSet()
        val format = DateTimeFormatter.ofPattern(configuration.timestampFormat).withZone(ZoneId.systemDefault())

        var empty = true

        listFiles().map { file ->
            Files.newByteChannel(file, StandardOpenOption.READ).use { channel ->
                val bytes = ByteBuffer.allocateDirect(channel.size().toInt())
                channel.read(bytes)
                bytes.flip()
                val chars = Charset.forName(configuration.fileCharset).decode(bytes)
                regex.findAll(chars).forEach { match ->
                    empty = false
                    entryHandler(
                        FilePatternLogEntry(
                            ZonedDateTime.parse(match.groups["timestamp"]!!.value, format),
                            groups,
                            match.value,
                            groups.mapNotNull { name ->
                                match.groups[name]?.value?.let { name to it }
                            }.toMap()
                        )
                    )
                }
            }
        }

        return LogRepository.LogQueryResponse(logQuery, empty)
    }

    private fun listFiles(): List<Path> {
        val files = ArrayList<Path>()

        val baseDir = Paths.get(configuration.baseDir)

        val matcher = FilePatternConfiguration.filePatternToMatcher(configuration.filePattern)

        val visitor = object : SimpleFileVisitor<Path>() {
            fun match(file: Path) {
                val name = baseDir.relativize(file)
                if (matcher.matches(name)) {
                    files.add(file)
                }
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                match(file)
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(
                file: Path,
                exc: IOException
            ): FileVisitResult {
                logger.info(exc) { "Visiting $file" }
                return FileVisitResult.CONTINUE
            }
        }

        Files.walkFileTree(baseDir, visitor)

        return files
    }

    override fun get(uid: Nothing) = throw UnsupportedOperationException()

    override val entryKeyType = Nothing::class

    override fun close() {}
}
