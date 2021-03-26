package gma.logs.app.model.query

import gma.logs.app.model.query.StringFieldCriterion.Type.*

sealed class Criteria {
    abstract fun match(entry: LogEntry<*>): Boolean
    open fun and(other: Criteria): Criteria = AndCriteria(this, other)
    open fun or(other: Criteria): Criteria = OrCriteria(this, other)
}

class AndCriteria(vararg criteria: Criteria) : Criteria() {

    val criteria = criteria.asList()

    override fun match(entry: LogEntry<*>) = criteria.all { it.match(entry) }

    override fun and(other: Criteria) = AndCriteria(*criteria.toTypedArray(), other)

    override fun toString() = criteria.joinToString(" and ", "(", ")")
}

class OrCriteria(vararg criteria: Criteria) : Criteria() {

    val criteria = criteria.asList()

    override fun match(entry: LogEntry<*>) = criteria.any { it.match(entry) }

    override fun or(other: Criteria) = OrCriteria(*criteria.toTypedArray(), other)

    override fun toString() = criteria.joinToString(" or ", "(", ")")
}

interface Operator {
    val operator: String
    val hasSecondOperand: Boolean
}

class StringFieldCriterion
    (val type: Type, val name: String, val value: String?) :
    Criteria() {
    enum class Type(override val operator: String, override val hasSecondOperand: Boolean) : Operator {
        IS_EMPTY("is empty", false),
        IS_NOT_EMPTY("is not empty", false),
        EQ("==", true),
        NOT_EQ("!=", true),
        EQ_MATCH_CASE("===", true),
        NOT_EQ_MATCH_CASE("!==", true),
        MATCH("~=", true),
        MATCH_MATCH_CASE("~==", true),
        NOT_MATCH("!~=", true),
        NOT_MATCH_MATCH_CASE("!~==", true),
        GT(">", true),
        GT_EQ(">=", true),
        LT("<", true),
        LT_EQ("<=", true)
    }

    private val regex = if (type == MATCH || type == NOT_MATCH) {
        Regex(value!!, RegexOption.IGNORE_CASE)
    } else if (type == MATCH_MATCH_CASE || type == NOT_MATCH_MATCH_CASE) {
        Regex(value!!)
    } else null

    override fun match(entry: LogEntry<*>) =
        (entry[name] ?: "").toString().let {
            when (type) {
                IS_EMPTY -> it.isEmpty()
                IS_NOT_EMPTY -> it.isNotEmpty()
                EQ -> it.equals(value, true)
                NOT_EQ -> !it.equals(value, true)
                EQ_MATCH_CASE -> it == value
                NOT_EQ_MATCH_CASE -> it != value
                MATCH -> regex!!.matches(it)
                MATCH_MATCH_CASE -> regex!!.matches(it)
                NOT_MATCH -> !regex!!.matches(it)
                NOT_MATCH_MATCH_CASE -> !regex!!.matches(it)
                GT -> it > (value ?: "")
                GT_EQ -> it >= (value ?: "")
                LT -> it < (value ?: "")
                LT_EQ -> it <= (value ?: "")
            }
        }

    override fun toString() =
        "[$name $type" + (if (type.hasSecondOperand) " $value]" else "]")
}
