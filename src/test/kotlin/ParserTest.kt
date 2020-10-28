import ast.*
import parsing.AntlrParserFacade

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test as test

class ParserTest {
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
        val combined = ast.entities.filterIsInstance<DefinedVariables>()
        val total = combined.map { it.vars }.flatten()
        assertEquals(4, total.size)
        val initExp1 = total[0].initExp as IntLiteral
        assertEquals("0", initExp1.value)
        val initExp2 = total[1].initExp as IntLiteral
        assertEquals("10", initExp2.value)
        assertTrue { total[2].initExp == null }
    }

    @test
    fun variableInFunctionWithInitExpr() {
        val code = """int checkIf(void){
                    |   int a, b=10, c;
                    |   int max=20;
                    |   return max;
                    |}""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // check the entities
        val defun = ast.entities[0] as DefinedFunction
        val locals = defun.body.block.variables
        assertEquals(4, locals.size)
        val initExp1 = locals[1].initExp as IntLiteral
        assertEquals("10", initExp1.value)
        val initExp2 = locals[3].initExp as IntLiteral
        assertEquals("20", initExp2.value)
    }

    @test
    fun ifStatementWithoutElse() {
        val code = """int checkIf(void){
            |int a, b;
            |int max = b;
            |if(a >= b) {
            |   max = a;
            |   
            |}
        |}
        |return max;
        |}""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // it should have one defined function, which contains one if statement
        val defun = ast.entities[0] as DefinedFunction
        val ifStmt = defun.body.block.statements[0] as IfStatement
        val outerTrueStmt = ifStmt.tStmt as BlockStatment
        assertEquals(1, outerTrueStmt.block.statements.size) // true branch is a block, containing one single assignment statement
        assertNull(ifStmt.fStmt) // false branch is empty
    }

    @test
    fun ifStatementWithElse() {
        val code = """int checkIf(void){
            |int a, b, c;
            |int max;
            |if(a >= b) {
            |   if (a >= c){
            |       max = a;
            |   }else
            |       max = c;
            |}else
            |   if (b >= c)
            |       max = b;
            |   else
            |       max = c;
            |
        |}
        |return max;
        |}""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // it should have one defined function, which contains one if statement
        val defun = ast.entities[0] as DefinedFunction
        val ifStmt = defun.body.block.statements[0] as IfStatement
        val outerTrueStmt = ifStmt.tStmt as BlockStatment
        assertEquals(1, outerTrueStmt.block.statements.size) // true branch is a block, containing one single if statement
        assertTrue(ifStmt.fStmt is IfStatement) // false branch is an if-statement
    }

    @test
    fun whileStatementWithBlock() {
        val code = """int checkWhile(void){
            |int a=100, b=10;
            |int count = 0;
            |while(a >= b) {
            |   count = count + 1;
            |   a = a - b;
            |}
            |return count;
        |}""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // it should have one defined function, which contains one if statement
        val defun = ast.entities[0] as DefinedFunction
        val whileStmt = defun.body.block.statements[0] as WhileStatement
        assertTrue(whileStmt.condExpr is BinaryExp)
        val loopStmt = whileStmt.loopStmt as BlockStatment
        assertEquals(2, loopStmt.block.statements.size) // loop statement
    }

    @test
    fun whileStatementWithoutBlock() {
        val code = """int checkWhile(void){
            |int a=100, b=10;
            |while(a >= b) 
            |   a = a - b;
            |
            |return a;
        |}""".trimMargin("|")

        // AntlrParserFacade.parse(code).isCorrect() // should check whether there is any error?
        val ast = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        // it should have one defined function, which contains one if statement
        val defun = ast.entities[0] as DefinedFunction
        val whileStmt = defun.body.block.statements[0] as WhileStatement
        assertTrue(whileStmt.condExpr is BinaryExp)
        assertTrue(whileStmt.loopStmt is ExprStatement)
    }
}
// these tests should fail
// statements in the global scope