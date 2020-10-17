import ast.*
import parsing.AntlrParserFacade

import kotlin.test.assertEquals
import org.junit.Test as test

// need to add test for nested scopes
class AstTest {
    @test fun variableDeclaration() {
        val code = """int a;
                    |float f1, f2;
                    |int[4][4] matrix;
                    |struct Point pt1, pt2;
                    |struct Line line;""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // check the entities
        val combined = ast.entities.filter{ it is DefinedVariables}
        val total = combined.map{(it as DefinedVariables).vars}.flatten()
        assertEquals(7, total.size)
    }

    @test fun structMemberReference() {
        val code = """int function(struct Point pt1){
                    |int a, b;
                    |pt1.x = 10;
                    |pt1.x.y = 20;
                    |a = pt1.x;
                    |b = pt1.x.y;
                    |}""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // check the entities
        val defun = ast.entities[0] as DefinedFunction
        val stmt1 = defun.body.statements[0] as ExprStatement
        val exp1 = stmt1.expr as AssignExpr

        val expected = MemberExp(VariableExp("pt1"), "x")

        assertEquals(expected,  exp1.term)
    }

}