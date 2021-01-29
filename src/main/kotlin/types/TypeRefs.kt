package types
import me.tomassetti.kolasu.model.Position

// the reference to a type
// one can use a struct datatype to declare a variable, and define the structure later
// a reference has a position
abstract class TypeRef(val position : Position? = null){

}

abstract class NamedTypeRef(val name: String, position: Position? = null) : TypeRef(position){
    override fun equals(other: Any?): Boolean {
        if(this === other ) return true

        var equal = false
        if(other != null){
            if(this::class == other::class )
                equal = true
        }
        return equal
    }
    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class VoidTypeRef(name: String = "void", position: Position? = null) : NamedTypeRef(name, position)

class StringTypeRef(name: String = "string", position: Position? = null) : NamedTypeRef(name, position)

class IntegerTypeRef(name: String = "int", position: Position? = null) : NamedTypeRef(name, position){

}

class FloatTypeRef(name: String = "float", position: Position? = null) : NamedTypeRef(name, position)

class CharTypeRef(name: String = "char", position: Position? = null) : NamedTypeRef(name, position)

class ShortTypeRef(name: String = "short", position: Position? = null) : NamedTypeRef(name, position)

class StructTypeRef(name: String, position: Position? = null) : NamedTypeRef(name, position)

// array is derivative type, doesn't have a name
class ArrayTypeRef(val baseRef: TypeRef, val dims: IntArray, position: Position? = null) : TypeRef(position){
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        var equal = false
        if(other != null){
            if(other is ArrayTypeRef)
                if(this.baseRef::class == other.baseRef::class )
                    equal = true
        }
        return equal
    }
    override fun hashCode(): Int {
        return baseRef.hashCode()*31 + dims.contentHashCode()
    }
}

class DummyTypeRef(val name: String, position: Position?=null) : TypeRef(position)