package types
// define the types,
abstract class TypeDef{
    abstract fun size(): Int
    open fun isChar(): Boolean = false
    open fun isShort(): Boolean = false
    open fun isInt(): Boolean =  false
    open fun isFloat(): Boolean = false
    open fun isArray(): Boolean = false
    open fun isStruct(): Boolean = false
    open fun isFunction(): Boolean = false
}
// for the family of "int" types, including short, int, long
class IntegerType(val name:String = "int") : TypeDef()
{
    override fun size(): Int = 4 // 4 bytes, 32 bits, for now
    override fun isInt(): Boolean = true
}

class CharType(val name:String = "char") : TypeDef()
{
    override fun size(): Int = 1 // 1 bytes, 8 bits
    override fun isChar(): Boolean = true
}

class ShortType(val name:String = "short") : TypeDef()
{
    override fun size(): Int = 2 // 2 bytes, 16 bits
    override fun isShort(): Boolean = true
}

class FloatType(val name:String = "float") : TypeDef()
{
    override fun size(): Int = 4 // 4 bytes, 32 bits, for now
    override fun isFloat(): Boolean = true
}

// represent a member of struct, a member must have a name, and its type may reference to another type
// that is why it is TypeDesc type
class Member (val name: String, val typeDesc: TypeDesc)

class StructType(val name:String, var members:List<Member>) : TypeDef()
{
    override fun isStruct(): Boolean {
        return true
    }
    override fun size(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class ArrayType(val baseType: TypeDef, val dims: IntArray) : TypeDef(){
    override fun size(): Int{
        TODO("array size not implemented")
    }

    override fun isArray(): Boolean {
        return true
    }
}