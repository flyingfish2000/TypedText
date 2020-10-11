package parsing

import ast.DefinedFunction
import ast.toAst
import me.tomassetti.kolasu.model.multilineString
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.StringReader
import java.io.FileInputStream

import whu.typedtext.TypedTextLexer
import whu.typedtext.TypedTextParser

fun readExampleCode() = FileInputStream("examples/simpleTest.tt").bufferedReader().use { it.readText() }

fun lexerForCode(code: String) = TypedTextLexer(ANTLRInputStream(StringReader(code)))

fun parseCode(code: String) : TypedTextParser.Compilation_unitContext = TypedTextParser(CommonTokenStream(lexerForCode(code))).compilation_unit()

fun main(args: Array<String>) {
    val root=parseCode(readExampleCode()).toAst()
    for(ent in root.entities){
        if (ent is DefinedFunction){
            println(ent.body.multilineString())
        }
    }
    //println(root.multilineString())
    // readExampleCode is a simple function that read the code of our example file
    //println(toParseTree(parseCode(readExampleCode()), TypedTextParser.VOCABULARY).multiLineString())
}