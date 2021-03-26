package gma.commons.beans

import java.beans.Encoder
import java.beans.Expression
import java.beans.PersistenceDelegate
import java.beans.XMLEncoder
import java.util.UUID

object UUIDPersistenceDelegate : PersistenceDelegate() {
    fun register(xmlEncoder: XMLEncoder) =
        xmlEncoder.setPersistenceDelegate(UUID::class.java, this)

    override fun instantiate(oldInstance: Any?, out: Encoder?): Expression {
        val uuid = oldInstance as UUID
        return Expression(
            uuid, UUID::class.java, "fromString", arrayOf(uuid.toString())
        )
    }
}
