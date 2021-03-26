package gma.logs.app.model.config

import javafx.beans.property.StringProperty
import java.util.UUID

abstract class LogsConfiguration(val uuid: UUID, val category: ConfigurationCategory) {

    abstract val nameProperty: StringProperty
    abstract var name: String

    abstract fun copy(): LogsConfiguration

    override fun equals(other: Any?) =
        this === other || javaClass == other?.javaClass && uuid == (other as LogsConfiguration).uuid

    override fun hashCode() = uuid.hashCode()

}
