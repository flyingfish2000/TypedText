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

    val structTypes = typeTable.table.filterKeys { it is StructTypeRef }
    for((typeRef, typeDef) in structTypes){
        if(typeRef is StructTypeRef)
            resolveStructType(typeDef as StructType)
    }
    // for primitive type, if it is not defined, say
    // Integer a; the parser will flag this as syntax error
    // so only need resolve the struct type definition, i.e. each member is of valid type


    // resolve the type in all global variable declarations
    // structure type, array type,
    // function: return type, parameters, function body
    // literal values ??

    return errors
}