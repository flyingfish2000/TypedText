package types

// type description
// a veriable's type can be defined after it is used.
// example:
// struct Point pt;
// int a;
// in the AST the type of pt and a is a TypeDesc
// in the semantic validation, we need to resolve the typeDef member of the TypeDesc
class TypeDesc(val typeRef: TypeRef, var typeDef: TypeDef? = null)


