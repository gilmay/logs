package gma.commons.kt

private val namedGroupRegex = Regex("""\(\?<([a-zA-Z][a-zA-Z0-9]*)>""")

val Regex.namedGroups
    get() = namedGroupRegex.findAll(pattern).map { it.groups[1]!!.value }.toList()
