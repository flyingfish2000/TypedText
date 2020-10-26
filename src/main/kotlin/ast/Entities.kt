package ast

import types.TypeDesc

// abstract class to represent an entity that can be referenced to.
// for example, a variable is an entity, as it can be used in expressions or statements
// a structure definition is an entity, as it can used to define a variable
// a function definition is an entity, as it can be referenced (invoked)
abstract class Entity(var refCount:Int = 0)

// a variable has a name and type
// todo: a variable can have an initial expression
open class DefinedVariable(val name: String, val typeDesc: TypeDesc) : Entity(0){
    var initExp : Expression? = null
}

class DefinedVariables(val vars: List<DefinedVariable>): Entity(0)

// todo: a function definition needs returned type, parameters and a body
// conceptually, a parameter is like a variable declared in the function scope
class Parameter (name: String, typeDesc: TypeDesc) : DefinedVariable(name, typeDesc)
{
    fun isParam():Boolean = true
}
class DefinedFunction(val name: String, val returnType: TypeDesc, val params: List<Parameter>, val body: Container) : Entity(0)

// todo: a structure definition needs a member list, which is a list of DefinedVariables(?)
class DefinedStruct(val name: String, val typeDesc: TypeDesc) : Entity(0)

class UnknownEntity():Entity(0)
