package ast

import me.tomassetti.kolasu.model.Position
import me.tomassetti.kolasu.validation.Error
import types.ArrayType
import types.StructType
import types.TypeDef
import java.util.*

/**
 * validate the following things
 * struct member reference points to the valid struct member, a.b.c[2]
 * array member reference, the variable is an array, i.e. a[2],
 * the left of assignment must be valid LHS, i.e. a = fun(3) is valid, but fun(3) = 5 is not
 *
 * to do it, we need to iterate through the AST and look for assignment statement, and every MemberExp and ArrayRefExp
 *
 */

fun Compilation_unit.validate(typeTable: TypeTable): List<Error> {
    val errors = LinkedList<Error>()

    // check the init expression
    fun validateVariable(variable: DefinedVariable){

    }

    // check the structure member reference
    fun checkCompositeExpr(expr: Expression): TypeDef?{
        if(expr is VariableExp){
            // get the definition from the type table
            val varDef = expr.definition!! as DefinedVariable
            return varDef.typeDesc.typeDef
        } else if(expr is ArrayRefExp){
            // check the index first, IntLiteral no need to check
            if(!(expr.idxExp is IntLiteral))
                checkCompositeExpr(expr.idxExp)
            var targetType = checkCompositeExpr(expr.varExp)
            if(targetType != null){
                if(targetType is ArrayType)
                    return targetType.baseType
                else
                    errors.add(Error("Validation Check: expression ${expr.varExp.toString()} is not array type.", null))
            } else{
                // varExp is not of Array type
                errors.add(Error("Validation Check: expression ${expr.varExp.toString()} is not array type: ", expr.varExp.position))
            }
        } else if (expr is MemberExp){
            var member = expr.member
            var structType = checkCompositeExpr(expr.varExp) //as StructType?
            if(structType != null){
                if(structType is StructType) {
                    var memberDef = structType.members.find { it.name == member }
                    if (memberDef != null) {
                        return memberDef.typeDesc.typeDef
                    } else
                        errors.add(Error("Validation Check: member ${member} is not a valid member: ", expr.position))
                }else
                    errors.add(Error("Validation Check: expression is not a valid struct type: ", expr.position))
            }else
                errors.add(Error("Validation Check: expression is not struct type: ", expr.varExp.position))
        }
        //throw IllegalArgumentException("illegal member or type in expression.")
        return null
    }

    fun validateExpression(expr: Expression) {
        when (expr){
            is VariableExp ->{

            }
            is MemberExp -> {
                try {
                    checkCompositeExpr(expr)
                }catch (e: IllegalArgumentException){null}
            }
            is ArrayRefExp ->{
                checkCompositeExpr(expr)
            }
            is FuncallExpr ->{
                //resolveExpression(expr.funExp, symbolTable)
                //expr.args.forEach{
                //    resolveExpression(it, symbolTable)
                //}
            }
            is AssignExpr ->{
                // check the LHS first
                checkCompositeExpr(expr.term)
                validateExpression(expr.valExp)
            }
            is BinaryExp -> {
                validateExpression(expr.leftExp)
                if(expr.rightExp!=null)
                    validateExpression(expr.rightExp)
            }
        }
    }

    fun validateBlockStatement(blkStmt: BlockStatment){
        for(variable in blkStmt.block.variables)
            validateVariable(variable)
        for(stmt in blkStmt.block.statements) {
            when (stmt) {
                is ExprStatement -> {
                    validateExpression(stmt.expr)
                }
                is IfStatement -> {

                }
                is WhileStatement -> {

                }
                is RtnStatement ->{

                }
                is BlockStatment ->
                    validateBlockStatement(stmt)
            }
        }
    }

    fun validateFunc(funcDef: DefinedFunction){
        validateBlockStatement(funcDef.body)
    }
    // iterate the AST
    for(entity in this.entities){
        when (entity){
            is DefinedVariables ->{
                for(variable in entity.vars)
                    validateVariable(variable)
            }
            is DefinedVariable -> validateVariable(entity)
            is DefinedFunction -> {
                // function: return type, parameters, function body
                validateFunc(entity)
            }
        }
    }

    return errors
}