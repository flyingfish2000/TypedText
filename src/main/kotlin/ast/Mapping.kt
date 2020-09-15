package ast

import me.tomassetti.kolasu.mapping.ParseTreeToAstMapper
import me.tomassetti.kolasu.mapping.toPosition
import whu.typedtext.TypedTextParser.*

class MiniCalcParseTreeToAstMapper : ParseTreeToAstMapper<Compilation_unitContext, Compilation_unit> {
    override fun map(parseTreeNode: Compilation_unitContext): Compilation_unit = parseTreeNode.toAst()
}

// top_def : defun
//        | defvars
//        | defstruct
//
fun Top_defContext.toAst(considerPosition: Boolean = false) : Top_def {
    // check the content in the Top_defContxt
    when(this){
        is DefvarsContext ->{
            println("variable definitions")
        }
        is DefstructContext ->{
            println("Struct definition")
        }
        is DefunContext -> {
            println("function definition")
        }
        else -> {
            println("unexpected statement")
        }
    }
    return Top_def()
}

fun Compilation_unitContext.toAst(considerPosition: Boolean = false) : Compilation_unit {
    val topDefs = this.top_def().map { it.toAst(considerPosition)}
    return Compilation_unit(toPosition(considerPosition))
}
