package ast

import me.tomassetti.kolasu.mapping.ParseTreeToAstMapper
import me.tomassetti.kolasu.mapping.toPosition
import types.*
import whu.typedtext.TypedTextParser.*

class TypedTextParseTreeToAstMapper : ParseTreeToAstMapper<Compilation_unitContext, Compilation_unit> {
    override fun map(parseTreeNode: Compilation_unitContext): Compilation_unit = parseTreeNode.toAst()
}

fun TypeContext.toAst(considerPosition: Boolean = false) : TypeDesc{
    return this.typeref().toAst()
}

fun TyperefContext.toAst(onsiderPosition: Boolean = false): TypeDesc{
    val typeRef = this.typeref_base().toAst()
    if(this.dimens.count() > 0){
        println("array data type")
    }
    return TypeDesc( typeRef)
}

fun Typeref_baseContext.toAst(onsiderPosition: Boolean = false): TypeRef{
    // create typeref based on type name, VOID, CHAR, STRUCT IDENTIFIER
    when(this){
        is VoidTypeContext -> {
            println("Void type")
        }
        is IntTypeContext -> {
            println("Integer type")
            return IntegerTypeRef("int", null)
        }
        is FloatTypeContext ->{
            println("float type")
            return FloatTypeRef("float", null)
        }
        is StructTypeContext -> {
            return StructTypeRef(this.IDENTIFIER().text, null)
        }
    }
    return DummyTypeRef("Unsupported", null)
}

// create the description of the structure
fun DefstructContext.toAst(onsiderPosition: Boolean = false): DefinedStruct{
    val structName = this.name().IDENTIFIER().text
    val members = this.member_list().toAst()
    val structRef = StructTypeRef(structName, null)
    var structDef = StructType(structName, members)
    return DefinedStruct(structName, TypeDesc(structRef, structDef))
}

/*
return a list of "members", which are the members of a struct
*/
fun Member_listContext.toAst(onsiderPosition: Boolean = false): List<Member>{
    return this.slots.map{it.toAst()}
/*
    val members = mutableListOf<Member>()
    for (slot in this.slots){
        members.add(slot.toAst())
    }
    return members
 */
}

// each member has a type and name
// for the type, should create typeDesc
fun SlotContext.toAst(onsiderPosition: Boolean = false): Member{
    val typeDesc = this.type().toAst()
    val name = this.name().IDENTIFIER().text
    return Member(name, typeDesc)
}


// top_def : defun
//        | defvars
//        | defstruct
//
fun Top_defContext.toAst(considerPosition: Boolean = false) : Entity {
    // check the content in the Top_defContxt
    when(this){
        is DefvariablesContext ->{
            val varType = this.defvars().type().toAst()
            val vars = this.defvars().vars
            // for each vars, create a DefinedVariable
            var definedVars = mutableListOf<DefinedVariable>()
            for(variable in vars){
                val defVar = DefinedVariable(variable.text, varType)
                definedVars.add(defVar)
            }
            return DefinedVariables(definedVars.toList())
        }
        is DefstructureContext ->{
            println("Struct definition: " + this.text)
            return this.defstruct().toAst()
        }
        is DefunctionContext -> {
            println("function definition")
            return DefinedFunction("function")
        }
        else -> {
            println("unexpected statement")
            return UnknownEntity()
        }
    }
}

fun Compilation_unitContext.toAst(considerPosition: Boolean = false) : Compilation_unit {
    val entities = this.topDefs.map { it.toAst(considerPosition)}
    return Compilation_unit(entities, toPosition(considerPosition))
}
