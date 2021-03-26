package  gma.commons.kt

private val blanks = Regex("[ \t]+")
fun String.collapseBlanks(): String {
    return this.replace(blanks, " ")
}
