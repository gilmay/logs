package gma.logs.app.model.query

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.text.ParseException

internal class CriteriaParserTest {

    @Test
    fun `blank string parses to null`() {
        assertNull(CriteriaParser("  \t \n  ").parse())
    }

    @Test
    fun `empty string parses to null`() {
        assertNull(CriteriaParser("").parse())
    }

    @Test
    fun `parse equals`() {
        val c0 = CriteriaParser(" a == 123").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("a", c.name)
        assertEquals(StringFieldCriterion.Type.EQ, c.type)
        assertEquals("123", c.value)
    }

    @Test
    fun `parse not equals`() {
        val c0 = CriteriaParser("\"x\" != 123").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("x", c.name)
        assertEquals(StringFieldCriterion.Type.NOT_EQ, c.type)
        assertEquals("123", c.value)
    }

    @Test
    fun `parse equals match case`() {
        val c0 = CriteriaParser(" a === \"\\\"\"").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("a", c.name)
        assertEquals(StringFieldCriterion.Type.EQ_MATCH_CASE, c.type)
        assertEquals("\"", c.value)
    }

    @Test
    fun `parse not equals match case`() {
        val c0 = CriteriaParser("123 !== a:b").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("123", c.name)
        assertEquals(StringFieldCriterion.Type.NOT_EQ_MATCH_CASE, c.type)
        assertEquals("a:b", c.value)
    }

    @Test
    fun `parse matches`() {
        val c0 = CriteriaParser(" a ~= 123").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("a", c.name)
        assertEquals(StringFieldCriterion.Type.MATCH, c.type)
        assertEquals("123", c.value)
    }

    @Test
    fun `parse not matches`() {
        val c0 = CriteriaParser("\"x\" !~= 123").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("x", c.name)
        assertEquals(StringFieldCriterion.Type.NOT_MATCH, c.type)
        assertEquals("123", c.value)
    }

    @Test
    fun `parse matches match case`() {
        val c0 = CriteriaParser(" a ~== \"\\\"\"").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("a", c.name)
        assertEquals(StringFieldCriterion.Type.MATCH_MATCH_CASE, c.type)
        assertEquals("\"", c.value)
    }

    @Test
    fun `parse not matches match case`() {
        val c0 = CriteriaParser("123 !~= a:b").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("123", c.name)
        assertEquals(StringFieldCriterion.Type.NOT_MATCH, c.type)
        assertEquals("a:b", c.value)
    }

    @Test
    fun `parse greater than`() {
        val c0 = CriteriaParser("123 > a:b").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("123", c.name)
        assertEquals(StringFieldCriterion.Type.GT, c.type)
        assertEquals("a:b", c.value)
    }

    @Test
    fun `parse greater than or equal to`() {
        val c0 = CriteriaParser("123 >= 0").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("123", c.name)
        assertEquals(StringFieldCriterion.Type.GT_EQ, c.type)
        assertEquals("0", c.value)
    }

    @Test
    fun `parse less than`() {
        val c0 = CriteriaParser("123 < a:.b").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("123", c.name)
        assertEquals(StringFieldCriterion.Type.LT, c.type)
        assertEquals("a:.b", c.value)
    }

    @Test
    fun `parse less than or equal to`() {
        val c0 = CriteriaParser("_ <= -").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("_", c.name)
        assertEquals(StringFieldCriterion.Type.LT_EQ, c.type)
        assertEquals("-", c.value)
    }

    @Test
    fun `parse is empty`() {
        val c0 = CriteriaParser("_ is empty").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("_", c.name)
        assertEquals(StringFieldCriterion.Type.IS_EMPTY, c.type)
    }

    @Test
    fun `parse is not empty`() {
        val c0 = CriteriaParser("a.b.c is not empty").parse()
        assertTrue(c0 is StringFieldCriterion)

        val c = c0 as StringFieldCriterion
        assertEquals("a.b.c", c.name)
        assertEquals(StringFieldCriterion.Type.IS_NOT_EMPTY, c.type)
    }

    @Test
    fun `parse complex expression`() {
        val string = """
            one is not empty
                && 2 is empty
                and (3 == 3 oR four!==4)
                || "five"===5
                OR 6!=  "6"
        """.trimIndent()

        val c0 = CriteriaParser(string).parse()
        assertTrue(c0 is OrCriteria)
        assertEquals(3, (c0 as OrCriteria).criteria.size)

        assertTrue(c0.criteria[0] is AndCriteria)
        val c1 = c0.criteria[0] as AndCriteria
        assertEquals(2, c1.criteria.size)

        assertTrue(c1.criteria[0] is StringFieldCriterion)
        assertEquals("one", (c1.criteria[0] as StringFieldCriterion).name)
        assertEquals(StringFieldCriterion.Type.IS_NOT_EMPTY, (c1.criteria[0] as StringFieldCriterion).type)
        assertNull((c1.criteria[0] as StringFieldCriterion).value)

        assertTrue(c1.criteria[1] is AndCriteria)
        val c2 = c1.criteria[1] as AndCriteria
        assertEquals(2, c2.criteria.size)

        assertTrue(c2.criteria[0] is StringFieldCriterion)
        assertEquals("2", (c2.criteria[0] as StringFieldCriterion).name)
        assertEquals(StringFieldCriterion.Type.IS_EMPTY, (c2.criteria[0] as StringFieldCriterion).type)
        assertNull((c2.criteria[0] as StringFieldCriterion).value)

        assertTrue(c2.criteria[1] is OrCriteria)
        val c3 = c2.criteria[1] as OrCriteria
        assertEquals(2, c3.criteria.size)

        assertTrue(c3.criteria[0] is StringFieldCriterion)
        assertEquals("3", (c3.criteria[0] as StringFieldCriterion).name)
        assertEquals(StringFieldCriterion.Type.EQ, (c3.criteria[0] as StringFieldCriterion).type)
        assertEquals("3", (c3.criteria[0] as StringFieldCriterion).value)

        assertTrue(c3.criteria[1] is StringFieldCriterion)
        assertEquals("four", (c3.criteria[1] as StringFieldCriterion).name)
        assertEquals(StringFieldCriterion.Type.NOT_EQ_MATCH_CASE, (c3.criteria[1] as StringFieldCriterion).type)
        assertEquals("4", (c3.criteria[1] as StringFieldCriterion).value)

        assertTrue(c0.criteria[1] is StringFieldCriterion)
        assertEquals("five", (c0.criteria[1] as StringFieldCriterion).name)
        assertEquals(StringFieldCriterion.Type.EQ_MATCH_CASE, (c0.criteria[1] as StringFieldCriterion).type)
        assertEquals("5", (c0.criteria[1] as StringFieldCriterion).value)

        assertTrue(c0.criteria[2] is StringFieldCriterion)
        assertEquals("6", (c0.criteria[2] as StringFieldCriterion).name)
        assertEquals(StringFieldCriterion.Type.NOT_EQ, (c0.criteria[2] as StringFieldCriterion).type)
        assertEquals("6", (c0.criteria[2] as StringFieldCriterion).value)

    }

    @Test
    fun `unclosed string fails`() {
        assertThrowsParseException("Unclosed string", 3) {
            CriteriaParser("\"abc").parse()
        }
    }

    @Test
    fun `unsupported escape sequence fails`() {
        assertThrowsParseException("Illegal escape sequence: '\\a'", 2) {
            CriteriaParser(" \"\\a\"").parse()
        }
    }

    @Test
    fun `unexpected value fails`() {
        assertThrowsParseException("Unexpected: '1'", 12) {
            CriteriaParser("(a is empty 100)").parse()
        }
    }

    private fun assertThrowsParseException(message: String, errorOffset: Int, block: () -> Unit) {
        try {
            block()
        } catch (e: ParseException) {
            assertEquals(message, e.message)
            assertEquals(errorOffset, e.errorOffset)
            return
        }
        fail("Expected ParseException(\"$message\", $errorOffset)")
    }
}
