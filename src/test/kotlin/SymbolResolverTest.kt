import ast.*
import org.junit.Test
import parsing.AntlrParserFacade
import kotlin.test.assertEquals

class SymbolResolverTest {
    @Test
    fun resolveGlobalVariable() {
        val code = """struct Point {
        |   int x;
        |   int y;
        |};
        |int gInt = 1;
        |int average(int a, int b){
        |   return (a+b)/2;
        | }
        |int main (int args)
        |{
        |   struct Point pt; 
        |   gInt = 10;
        |   if(gInt < 5){
        |       int localInt = 1;
        |       while(gInt < 5){
        |           localInt = localInt + gInt + average(localInt, gInt);
        |       }
        |   }
        |}""".trimMargin("|")
        val root = AntlrParserFacade.parse(code).root!!.toAst() // result is Compilation_unit

        val errors = root.resolveSymbols()

        errors.forEach {
            println("${it.message}, at ${it.position}")
        }
        assertEquals(0, errors.size) // no errors
    }
}