package ast

// abstract class to represent an entity that can be referenced to.
// for example, a variable is an entity, as it can be used in expressions or statements
// a structure definition is an entity, as it can used to define a variable
// a function definition is an entity, as it can be referenced (invoked)
abstract class Entity(var refCount:Int = 0)

// todo: a variable has a name and type
class DefinedVariable(val name: String) : Entity(0)

// todo: a function definition needs returned type, parameters and a body
class DefinedFunction(val name: String) : Entity(0)

// todo: a structure definition needs a member list, which is a list of DefinedVariables(?)
class DefinedStruct(val name: String) : Entity(0)

