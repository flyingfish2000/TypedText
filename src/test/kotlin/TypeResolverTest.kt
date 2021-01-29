import ast.*
import org.junit.Test
import parsing.AntlrParserFacade
import kotlin.test.assertEquals

class TypeResolverTest {
    @Test
    fun resolveStringVariable() {
        val code = """
        |int main (int args)
        |{
        |   string info;
        |   string info2 = "Hello, World";
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit

        var typeTable = TypeTable()
        val errors = root.resolveTypes(typeTable)

        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(0, errors.size) // no errors
    }
}