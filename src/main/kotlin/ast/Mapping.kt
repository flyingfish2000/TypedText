package ast

import me.tomassetti.kolasu.mapping.ParseTreeToAstMapper
import me.tomassetti.kolasu.mapping.toPosition
import types.*
import whu.typedtext.TypedTextParser.*

/*
class TypedTextParseTreeToAstMapper : ParseTreeToAstMapper<Compilation_unitContext, Compilation_unit> {
    override fun map(parseTreeNode: Compilation_unitContext): Compilation_unit = parseTreeNode.toAst()
}
*/

fun TypeContext.toAst(withPos: Boolean = false) : TypeDesc{
    return this.typeref().toAst(withPos)
}

fun TyperefContext.toAst(withPos: Boolean = false): TypeDesc{
    val typeRef = this.typeref_base().toAst(withPos)
    if(this.dimCount > 0){ // if(this.dimens.count() > 0){
        val dims = this.dimens.map{it.text.toInt()}
        val arrayTypeRef = ArrayTypeRef(typeRef, dims.toIntArray(), toPosition(withPos))
        return TypeDesc(arrayTypeRef)
    }
    return TypeDesc( typeRef)
}

fun Typeref_baseContext.toAst(withPos: Boolean = false): TypeRef{
    // create typeref based on type name, VOID, CHAR, STRUCT IDENTIFIER
    when(this){
        is VoidTypeContext -> {
            println("Typeref_base: Void type")
        }
        is IntTypeContext -> {
            //println("Integer type")
            return IntegerTypeRef("int", toPosition(withPos))
        }
        is FloatTypeContext ->{
            //println("float type")
            return FloatTypeRef("float", toPosition(withPos))
        }
        is StructTypeContext -> {
            return StructTypeRef(this.IDENTIFIER().text, toPosition(withPos))
        }
    }
    return DummyTypeRef("Unsupported", null)
}

// create the description of the structure
fun DefstructContext.toAst(withPos: Boolean = false): DefinedStruct{
    val structName = this.name().IDENTIFIER().text
    val members = this.member_list().toAst(withPos)
    val structRef = StructTypeRef(structName, toPosition(withPos))
    val structDef = StructType(structName, members)
    return DefinedStruct(structName, TypeDesc(structRef, structDef))
}

/*
return a list of "members", which are the members of a struct
*/
fun Member_listContext.toAst(withPos: Boolean = false): List<Member>{
    return this.slots.map{it.toAst(withPos)}
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
fun SlotContext.toAst(withPos: Boolean = false): Member{
    val typeDesc = this.type().toAst(withPos)
    val name = this.name().IDENTIFIER().text
    return Member(name, typeDesc)
}

// function parameters
fun ParamContext.toAst(withPos: Boolean = false): Parameter{
    val typeDesc = this.type().toAst(withPos)
    val name = this.name().IDENTIFIER().text
    return Parameter(name, typeDesc)
}

// parameters can be Void or a list of parameters, either way, can safely call to.Ast()
// no need to check the type using when
fun ParamsContext.toAst(withPos: Boolean = false): List<Parameter>{
    return if (this is ParamListContext){
        this.toAst(withPos)
    }else {
        // return an empty list
        listOf<Parameter>()
    }
}

fun ParamListContext.toAst(withPos: Boolean = false): List<Parameter>{
    return this.plist.map{it.toAst(withPos)}
}

fun ParamEmptyContext.toAst(withPos: Boolean = false): List<Parameter>{
    return listOf<Parameter>() // return emptyList()
}

fun DefunContext.toAst(withPos: Boolean = false) : DefinedFunction{
    val rtnType = this.typeref().toAst(withPos) // the return type
    val funName = this.name().text // function name
    val params = this.params().toAst(withPos) // function params
    val body = this.block().toAst(withPos)
    return DefinedFunction(funName, rtnType, params, body)
}

// a list of variables with the same type
// each variable my have an optional init expression
fun DefvarsContext.toAst(withPos: Boolean = false): List<DefinedVariable>{
    val varType = this.type().toAst(withPos)
    var defVars = this.vars.map{DefinedVariable(it.text, varType)}
    for((idx, initExp) in this.inits.withIndex() ){
        val varIdx = this.indices[idx]
        defVars[varIdx].initExp = initExp.toAst(withPos)
    }
    return defVars
}

// a block can have many variables with different types
fun Defvar_listContext.toAst(withPos: Boolean = false) : List<DefinedVariable>{
    val allVars = this.defvars().map{it.toAst(withPos)}
    return allVars.flatten()
}

fun BlockContext.toAst(withPos: Boolean = false) : Container{
    val vars = this.defvar_list().toAst(withPos)
    // statement
    var stmts = this.stmts().toAst(withPos)
    return Container(vars, stmts)
}

fun StmtsContext.toAst(withPos: Boolean = false) : List<Statement>{
    return this.stmt().map{it.toAst(withPos)}
}

fun StmtContext.toAst(withPos: Boolean = false): Statement{
    when (this){
        is ExprStmtContext ->{
           return ExprStatement(this.expr().toAst(withPos))
        }
        is EptStmtContext -> {
            return DummyStatment("empty")
        }
        is BlockStmtContext -> {
            val container = this.block().toAst(withPos)
            return BlockStatment(container)
        }
        is IfStmtContext -> {
            return this.toAst(withPos) //DummyStatment("if")
        }
        is RtnStmtContext -> {
            return DummyStatment("return")
        }
        is WhileStmtContext -> {
            return DummyStatment("while")
        }
        else ->
            return DummyStatment("Unknown")
    }
}

fun If_stmtContext.toAst(withPos: Boolean = false):IfStatement{
    val condExp = this.expr().toAst(withPos)
    val trueStmt = this.tstmt.toAst(withPos)
    val falseStmt = this.fstmt.toAst(withPos)
    return IfStatement(condExp, trueStmt, falseStmt)
}

fun ExprContext.toAst(withPos: Boolean = false):Expression{
    when (this){
        is SubExpr5Context ->{
            return this.expr5().toAst(withPos)
        }
        is AssignContext -> {
            val termExp = this.term().toAst(withPos)
            val valExp = this.expr().toAst(withPos)
            return AssignExpr(termExp, valExp)
        }
        else ->
            return UnknownExp()
    }
}

// logical ||
fun Expr5Context.toAst(withPos: Boolean = false) : Expression{
    val leftExp = this.left.toAst(withPos)
    if(this.right.isEmpty()){
        return leftExp
    }else {
        var binExp = BinaryExp(leftExp, "||", this.right[0].toAst(withPos), toPosition(withPos)) // when it is not empty, at least has one element
        for(idx in 1 until this.right.size) {
            binExp = BinaryExp(binExp, "||", this.right[idx].toAst(withPos))
        }
        return binExp
    }
}


fun Expr4Context.toAst(withPos: Boolean = false) : Expression{
    val leftExp = this.left.toAst(withPos)
    if(this.right.isEmpty()){
        return leftExp
    }else {
        var binExp = BinaryExp(leftExp, "&&", this.right[0].toAst(withPos), toPosition(withPos)) // when it is not empty, at least has one element
        for(idx in 1 until this.right.size) {
            binExp = BinaryExp(binExp, "&&", this.right[idx].toAst(withPos))
        }
        return binExp
    }
}

fun Expr3Context.toAst(withPos: Boolean = false) : Expression{
    val leftExp = this.left.toAst(withPos)
    if(this.op.isEmpty()){
        return leftExp
    }else {
        var binExp = BinaryExp(leftExp, this.op[0].text, this.right[0].toAst(withPos), toPosition(withPos)) // when it is not empty, at least has one element
        for(idx in 1 until this.op.size) {
            binExp = BinaryExp(binExp, this.op[idx].text, this.right[idx].toAst(withPos))
        }
        return binExp
    }
}


// test a + b - c
fun Expr2Context.toAst(withPos: Boolean = false) : Expression{
    val leftExp = this.left.toAst(withPos)
    if(this.op.isEmpty()){
        return leftExp
    }else {
        var binExp = BinaryExp(leftExp, this.op[0].text, this.right[0].toAst(withPos), toPosition(withPos)) // when it is not empty, at least has one element
        for(idx in 1 until this.op.size) {
            binExp = BinaryExp(binExp, this.op[idx].text, this.right[idx].toAst(withPos))
        }
        return binExp
    }
}

// test a * b / c % d, left associative
fun Expr1Context.toAst(withPos: Boolean = false) : Expression{
    val leftExp = this.left.toAst(withPos)
    if(this.op.isEmpty()){
        return leftExp
    }else {
        var binExp = BinaryExp(leftExp, this.op[0].text, this.right[0].toAst(withPos), toPosition(withPos)) // when it is not empty, at least has one element
        for(idx in 1 until this.op.size) {
            binExp = BinaryExp(binExp, this.op[idx].text, this.right[idx].toAst(withPos))
        }
        return binExp
    }
}

// test: a.b.c, a.b[3], a.b[x], fun(x, y+z, 4+5)
fun TermContext.toAst(withPos: Boolean = false): Expression{
    val primExp = this.primary().toAst(withPos)
    var termExp = primExp
    for(ptfix in this.posfixes){
        when (ptfix){
            is StructMemberContext -> {
                termExp = MemberExp(termExp, ptfix.name().text, toPosition(withPos))
            }
            is ArrayMemberContext -> {
               termExp = ArrayRefExp(termExp, ptfix.expr().toAst(withPos), toPosition(withPos))
            }
            is FunCallContext -> {
                termExp = FuncallExpr(termExp, ptfix.args().toAst(withPos), toPosition(withPos))
            }
        }
    }
    return termExp
}

// function call arguments
fun ArgsContext.toAst(withPos: Boolean=false) : List<Expression>{
    return funArgs.map{it.toAst(withPos)}
}

fun PrimaryContext.toAst(withPos: Boolean = false): Expression{
    when (this){
        is IntLitContext -> {
            return IntLiteral(this.INTEGER_NUM().text, toPosition(withPos))
        }
        is FloatLitContext -> {
            return FloatLiteral(this.FLOAT_NUM().text, toPosition(withPos))
        }
        is PrimarIdContext -> {
            return VariableExp(this.IDENTIFIER().text, toPosition(withPos))
        }
        is PrimaryExpContext ->{
            return this.expr().toAst(withPos)
        }
        else ->
            return UnknownExp()
    }
}

// top_def : defun
//        | defvars
//        | defstruct
//
fun Top_defContext.toAst(withPos: Boolean = false) : Entity {
    // check the content in the Top_defContxt
    when(this){
        is DefvariablesContext ->{
            val vars = this.defvars().toAst()
            //val varType = this.defvars().type().toAst(withPos)
            //val vars = this.defvars().vars.map{DefinedVariable(it.text, varType)}
            return DefinedVariables(vars)
        }
        is DefstructureContext ->{
            //println("Struct definition: " + this.text)
            return this.defstruct().toAst(withPos)
        }
        is DefunctionContext -> {
            //println("function definition")
            return this.defun().toAst(withPos)
        }
        else -> {
            println("Top_def: unexpected statement")
            return UnknownEntity()
        }
    }
}

fun Compilation_unitContext.toAst(withPos: Boolean = false) : Compilation_unit {
    val entities = this.topDefs.map { it.toAst(withPos)}
    return Compilation_unit(entities, toPosition(withPos))
}
