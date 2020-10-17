package types
import me.tomassetti.kolasu.model.Position

// the reference to a type
// one can use a struct datatype to declare a variable, and define the structure later
// a reference has a position
abstract class TypeRef(val position : Position? = null){

}

class IntegerTypeRef(val name: String, position: Position?) : TypeRef(position){

}

class FloatTypeRef(val name: String, position: Position?) : TypeRef(position)

class StructTypeRef(val name: String, position: Position?) : TypeRef(position)

class ArrayTypeRef(val baseRef: TypeRef, val dims: IntArray, position: Position?) : TypeRef(position)

class DummyTypeRef(val name: String, position: Position?) : TypeRef(position)