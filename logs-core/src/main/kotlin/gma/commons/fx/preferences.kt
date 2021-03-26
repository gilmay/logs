package gma.commons.fx

import tornadofx.*
import java.util.prefs.Preferences

fun preferences(op: Preferences.() -> Unit = {}): Preferences {
    val node = Preferences.userNodeForPackage(FX.getApplication()!!.javaClass)
    op(node)
    return node
}
