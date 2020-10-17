package parsing

import whu.typedtext.TypedTextLexer
import whu.typedtext.TypedTextParser
import whu.typedtext.TypedTextParser.*

import ast.Compilation_unit
import ast.toAst

import me.tomassetti.kolasu.model.Point
import me.tomassetti.kolasu.model.Position
import me.tomassetti.kolasu.parsing.ParsingResult
import me.tomassetti.kolasu.validation.Error

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*


data class AntlrParsingResult(val root : Compilation_unitContext?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

fun String.toStream(charset: Charset = Charsets.UTF_8) = ByteArrayInputStream(toByteArray(charset))

object AntlrParserFacade {

    fun parse(code: String) : AntlrParsingResult = parse(code.toStream())

    fun parse(file: File) : AntlrParsingResult = parse(FileInputStream(file))

    fun parse(inputStream: InputStream) : AntlrParsingResult {
        val lexicalAndSyntaticErrors = LinkedList<Error>()
        val errorListener = object : ANTLRErrorListener {
            override fun reportAmbiguity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Boolean, p5: BitSet?, p6: ATNConfigSet?) {
                // Ignored for now
            }

            override fun reportAttemptingFullContext(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: BitSet?, p5: ATNConfigSet?) {
                // Ignored for now
            }

            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInline: Int, msg: String, ex: RecognitionException?) {
                lexicalAndSyntaticErrors.add(Error(msg, Position(Point(line, charPositionInline), Point(line, charPositionInline))))
            }

            override fun reportContextSensitivity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Int, p5: ATNConfigSet?) {
                // Ignored for now
            }
        }

        val lexer = TypedTextLexer(ANTLRInputStream(inputStream))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val parser = TypedTextParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val antlrRoot = parser.compilation_unit()
        return AntlrParsingResult(antlrRoot, lexicalAndSyntaticErrors)
    }
}

object ParserFacade {
    fun parse(inputStream: InputStream, withValidation: Boolean): ParsingResult<Compilation_unit> {
        val code = inputStream.bufferedReader().use { it.readText() }
        val antlrParsingResult = AntlrParserFacade.parse(code)
        val lexicalAnsSyntaticErrors = antlrParsingResult.errors
        val antlrRoot = antlrParsingResult.root
        val astRoot = antlrRoot?.toAst(withPos = true) // need enable the position to report error location
        /* // validation comes later
        val semanticErrors = if (withValidation) {
            astRoot?.validate() ?: emptyList()
        } else {
            emptyList()
        }
         */
        return ParsingResult<Compilation_unit>(astRoot, lexicalAnsSyntaticErrors , code) // lexicalAnsSyntaticErrors + semanticErrors
    }

}