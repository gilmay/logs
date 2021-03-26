package gma.logs.app.model.query

import java.text.ParseException

class CriteriaParser(val string: String) {

    private var pos = -1
    private var ch = '\u0000'

    fun parse(): Criteria? {
        nextChar()
        skipBlanks()
        if (ch == '\u0000') return null
        val c = parseExpression()
        if (pos < string.length) throw ParseException("Unexpected: '$ch'", pos)
        return c
    }

    private fun nextChar(length: Int = 1) {
        pos += length
        ch = if (pos < string.length) string[pos] else '\u0000'
    }

    private fun skipBlanks() {
        while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') nextChar()
    }

    private fun tryRead(vararg expected: String): Boolean {
        skipBlanks()
        for (token in expected) {
            if (token.regionMatches(0, string, pos, token.length, ignoreCase = true)) {
                nextChar(token.length)
                return true
            }
        }
        return false
    }

    private fun parseExpression(): Criteria {
        var c = parseTerm()
        while (true) {
            if (tryRead("||", "or")) c = c.or(parseTerm())
            else return c
        }
    }

    private fun parseTerm(): Criteria {
        var c = parseFactor()
        while (true) {
            if (tryRead("&&", "and")) c = c.and(parseTerm())
            else return c
        }
    }

    private fun parseFactor(): Criteria {
        val c: Criteria
        if (tryRead("(")) {
            c = parseExpression()
            tryRead(")")
        } else {
            val operand =
                if (tryRead("\"")) {
                    parseString()
                } else {
                    parseWord()
                }

            var operator: StringFieldCriterion.Type? = null
            for (op in StringFieldCriterion.Type.values().sortedByDescending { it.operator.length }) {
                if (tryRead(op.operator)) {
                    operator = op
                    break
                }
            }

            c = when {
                operator == null -> throw ParseException(
                    "Operator expected (one of ${
                        StringFieldCriterion.Type.values().map { it.operator }
                    })", pos
                )
                operator.hasSecondOperand -> {
                    val secondOperand = if (tryRead("\"")) {
                        parseString()
                    } else {
                        parseWord()
                    }
                    StringFieldCriterion(operator, operand, secondOperand)
                }
                else -> {
                    StringFieldCriterion(operator, operand, null)
                }
            }
        }
        return c
    }

    private fun parseString(): String {
        val sb = StringBuilder()
        var escape = false

        while (pos < string.length) {
            when (ch) {
                '"' -> {
                    if (escape) {
                        escape = false
                        sb.append('"')
                    } else break
                }
                '\\' -> {
                    if (escape) {
                        escape = false
                        sb.append('\\')
                    } else escape = true
                }
                else -> {
                    if (escape) throw ParseException("Illegal escape sequence: '\\$ch'", pos - 1)
                    else sb.append(ch)
                }
            }
            nextChar()
        }
        if (ch != '"') {
            throw ParseException("Unclosed string", pos - 1)
        }
        nextChar()
        return sb.toString()
    }

    private fun parseWord(): String {
        val start = pos

        do {
            if (!ch.isWordPart) break
            nextChar()
        } while (pos < string.length)

        if (pos == start) {
            throw ParseException("Unexpected: '$ch'", pos)
        }

        return string.substring(start, pos)
    }

    companion object {
        val Char.isWordPart
            get() = isLetterOrDigit() || this in "-_.,:;[]'"

        fun escapeIfRequired(string: String) =
            if (string.none { !it.isWordPart }) string
            else '"' + string.replace("\\", "\\\\").replace("\"", "\\\"") + '"'
    }

}
