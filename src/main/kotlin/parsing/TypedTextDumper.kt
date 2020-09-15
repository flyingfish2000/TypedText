package parsing

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.StringReader
import java.io.FileInputStream

import whu.typedtext.TypedTextLexer
import whu.typedtext.TypedTextParser

fun readExampleCode() = FileInputStream("examples/test.tt").bufferedReader().use { it.readText() }

fun lexerForCode(code: String) = TypedTextLexer(ANTLRInputStream(StringReader(code)))

fun parseCode(code: String) : TypedTextParser.Compilation_unitContext = TypedTextParser(CommonTokenStream(lexerForCode(code))).compilation_unit()

fun main(args: Array<String>) {
    // readExampleCode is a simple function that read the code of our example file
    println(toParseTree(parseCode(readExampleCode()), TypedTextParser.VOCABULARY).multiLineString())
}