package ast

import me.tomassetti.kolasu.validation.Error
import types.*
import java.util.*
import kotlin.collections.HashMap

class TypeTable{
    val table = HashMap<TypeRef, TypeDef>()
    init { // the built-in types
        table[CharTypeRef()] = CharType()
        table[ShortTypeRef()] = ShortType()
        table[IntegerTypeRef()] = IntegerType()
        table[FloatTypeRef()] = FloatType()
    }

    fun getType(typeRef: TypeRef):TypeDef?{
        val typeDef = table[typeRef]

        if(typeDef == null){
            if(typeRef is ArrayTypeRef){
                // need to create a nameless type
                val baseDef = table[typeRef.baseRef] // get the type info from the type table
                if(baseDef != null){
                    val arrayType = ArrayType(baseDef, typeRef.dims)
                    table[typeRef] = arrayType
                    return arrayType
                } // if baseDef == null, Semantic error
            }
        }
        return typeDef
    }
    // TODO: need to check whether type with the same name has been added before
    // should return true if successfully added to the type table, false otherwise.
    fun addType(typeRef: TypeRef, typeDef: TypeDef){
        table[typeRef] = typeDef
    }
}

fun Compilation_unit.resolveTypes(): List<Error> {
    val errors = LinkedList<Error>()
    val typeTable = TypeTable()

    // get all structure definitions
    val structDefs = this.entities.filterIsInstance<DefinedStruct>()
    structDefs.forEach {
        if(it.typeDesc.typeDef != null) {
            typeTable.addType(it.typeDesc.typeRef, it.typeDesc.typeDef!!)
        }else{
            // this should not happen, as it is created when generating the AST
            errors.add(Error("TypeResolver: unable to resolve type of structure member ${it.name}", it.typeDesc.typeRef.position))
        }
    }

    fun resolveStructType(typeDef: StructType ){
        // check members
        for(member in typeDef.members) {
            val def = typeTable.getType(member.typeDesc.typeRef)
            if(def == null)
                errors.add(Error("TypeResolver: unable to resolve type of member: ${member.name}", member.typeDesc.typeRef.position))
            else
                member.typeDesc.typeDef = typeTable.getType(member.typeDesc.typeRef)
        }
    }

    // for primitive type, if it is not defined, say
    // Integer a; the parser will flag this as syntax error
    // so only need resolve the struct type definition, i.e. each member is of valid type
    val structTypes = typeTable.table.filterKeys { it is StructTypeRef }
    for((typeRef, typeDef) in structTypes){
        if(typeRef is StructTypeRef)
            resolveStructType(typeDef as StructType)
    }

    // check the type of each variable is defined, can be primitive type, structure type, or array of primitive or structure type
    fun resolveVarType(variable :DefinedVariable){
        val typeDesc = variable.typeDesc
        val typeDef = typeTable.getType(typeDesc.typeRef)
        if(typeDef == null)
            errors.add(Error("TypeResolver: unable to resolve type of variable: ${variable.name}", variable.typeDesc.typeRef.position))
        else
            variable.typeDesc.typeDef = typeDef
    }

    // block may contain local variable and sub blocks
    fun resolveBlockStatement(blkStmt: BlockStatment){
        for(variable in blkStmt.block.variables)
            resolveVarType(variable)
        for(stmt in blkStmt.block.statements) {
            when (stmt) {
                is IfStatement -> {
                    if (stmt.tStmt is BlockStatment)
                        resolveBlockStatement(stmt.tStmt)
                    else if (stmt.fStmt != null)
                        if (stmt.fStmt is BlockStatment)
                            resolveBlockStatement(stmt.fStmt)
                }
                is WhileStatement -> {
                    if (stmt.loopStmt is BlockStatment)
                        resolveBlockStatement(stmt.loopStmt)
                }
                is BlockStatment ->
                    resolveBlockStatement(stmt)
            }
        }
    }

    fun resolveFuncType(funcDef : DefinedFunction){
        // return type, each parameter, and local variables
        // return type can be Void, primitive or struct type, or array of either
        val retType = typeTable.getType(funcDef.returnType.typeRef)
        if(retType == null)
            errors.add(Error("TypeResolver: unable to resolve return type of function: ${funcDef.name}", funcDef.returnType.typeRef.position))
        else
            funcDef.returnType.typeDef = retType
        // check each params
        for(param in funcDef.params){
            val typeDef = typeTable.getType(param.typeDesc.typeRef)
            if(typeDef == null)
                errors.add(Error("TypeResolver: unable to resolve type of function parameter: ${funcDef.name}, ${param.name}", param.typeDesc.typeRef.position))
            else
                param.typeDesc.typeDef = typeDef
        }
        // check variables of local scope
        resolveBlockStatement(funcDef.body)
    }

    // resolve the type in all global variable declarations
    // structure type, array type,
    for(entity in this.entities){
        when (entity){
            is DefinedVariables ->{
                for(variable in entity.vars)
                    resolveVarType(variable)
            }
            is DefinedVariable -> resolveVarType(entity)
            is DefinedFunction -> {
                // function: return type, parameters, function body
                resolveFuncType(entity)
            }
        }
    }

    // literal values ??
    return errors
}