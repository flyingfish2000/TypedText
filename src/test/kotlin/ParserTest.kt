import ast.*
import parsing.AntlrParserFacade

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test as test

class ParserTestTest {
    // these tests should pass
    // global variable with init expression
    // function that takes array (int[]) as parameter

    @test
    fun variableDeclarationWithInitExpr() {
        val code = """int a=0;
                    |float f1=10, f2, f3=2*a;""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // check the entities
        val combined = ast.entities.filter { it is DefinedVariables }
        val total = combined.map { (it as DefinedVariables).vars }.flatten()
        assertEquals(4, total.size)
        val initExp1 = total[0].initExp as IntLiteral
        assertEquals("0", initExp1.value)
        val initExp2 = total[1].initExp as IntLiteral
        assertEquals("10", initExp2.value)
        assertTrue { total[2].initExp == null }
    }

}
// these tests should fail
// statements in the global scope