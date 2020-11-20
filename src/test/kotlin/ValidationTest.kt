import ast.*
import parsing.AntlrParserFacade

import kotlin.test.assertEquals
import org.junit.Test as test

// need to add test for nested scopes
class ValidationTest {
    @test
    fun checkValidMemberRef() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |struct Line {
        |   struct Point ptSrc;
        |   struct Point ptDest;
        |};
        |struct Segment{
        |   struct Line[10] pts;
        |};
        |struct Square{
        |   struct Point[4] pts;
        |};

        |int main (int args)
        |{
        |   struct Line line;
        |   line.ptDest.y = 3 * (line.ptDest.x + 10);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        assertEquals(0, errors.size) // valid, no error
    }

    @test
    fun checkInvalidMemberRef() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |struct Line {
        |   struct Point ptSrc;
        |   struct Point ptDest;
        |};
        |struct Segment{
        |   struct Line[10] pts;
        |};
        |struct Square{
        |   struct Point[4] pts;
        |};

        |int main (int args)
        |{
        |   struct Line line;
        |   line.ptDest.y = 3 * (line.ptDest.z + 10);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(1, errors.size) // invalid, z is not a valid member
    }

    @test
    fun checkValidArrayMemberRef() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |struct Line {
        |   struct Point ptSrc;
        |   struct Point ptDest;
        |};
        |struct Segment{
        |   struct Line[10] pts;
        |};
        |struct Square{
        |   struct Point[4] pts;
        |};

        |int main (int args)
        |{
        |   struct Segment sgmt;
        |   sgmt.pts[0].ptSrc.y = 10*(sgmt.pts[0].ptDest.x + 10);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(0, errors.size) // valid, no error
    }

    @test
    fun checkInvalidArrayMemberRef() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |struct Line {
        |   struct Point ptSrc;
        |   struct Point ptDest;
        |};
        |struct Segment{
        |   struct Line[10] pts;
        |};
        |struct Square{
        |   struct Point[4] pts;
        |};

        |int main (int args)
        |{
        |   struct Segment sgmt;
        |   sgmt.seg[0].ptSrc.x = 1;
        |   sgmt.pts[0].ptSrc.y = 10*(sgmt.pts[0].ptDes.x + 10);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(2, errors.size) // invalid, ptDes is not a member, seg is not a valid member
    }

    @test
    fun checkValidArrayRef() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |struct Line {
        |   struct Point ptSrc;
        |   struct Point ptDest;
        |};
        |struct Segment{
        |   struct Line[10] pts;
        |};
        |struct Square{
        |   struct Point[4] pts;
        |};

        |int main (int args)
        |{
        |   int[10] scores;
        |   int score;
        |   scores[9] = 10*(scores[0] + 10);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(0, errors.size) // valid, no error
    }

    @test
    fun checkInvalidArrayRef() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |struct Line {
        |   struct Point ptSrc;
        |   struct Point ptDest;
        |};
        |struct Segment{
        |   struct Line[10] pts;
        |};
        |struct Square{
        |   struct Point[4] pts;
        |};

        |int main (int args)
        |{
        |   int score;
        |   score[9] = 10*(score[0] + 10);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst(true) // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(2, errors.size) // invalid, score is not an array
    }

    @test
    fun checkInvalidLHS() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        | int average(int a, int b){
        |   return (a+b)/2;
        | }
        |int main (int args)
        |{
        |   int a = 10;
        |   int b, c;
        |   int average2 = 10;
        |   // b+c = a; // this is syntax error.
        |   10 = a;
        |   average = 10; // in theory, it is legal to assign a value (address) to a function variable.
        |   average(3, 4) = a;
        |   a = average2(b, c);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(3, errors.size) // two errors
    }

    @test
    fun checkValidLHS() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        | int average(int a, int b){
        |   return (a+b)/2;
        | }
        |int main (int args)
        |{
        |   struct Point pt; 
        |   int[10] a = 10;
        |   int b, c;
        |   a[10] = 15;
        |   pt.x = average(3, 4);
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit
        var typeTable = TypeTable()
        root.resolveTypes(typeTable)
        root.resolveSymbols()
        val errors = root.validate(typeTable)
        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(0, errors.size) // no errors
    }
}