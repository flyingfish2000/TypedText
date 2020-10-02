package types
// define the types,
abstract class TypeDef{
    abstract fun size(): Int
    open fun isInt(): Boolean =  false
    open fun isFloat(): Boolean = false
    open fun isArray(): Boolean = false
    open fun isStruct(): Boolean = false
    open fun isFunction(): Boolean = false
}
// for the family of "int" types, including short, int, long
class IntegerType(val name:String) : TypeDef()
{
    override fun size(): Int = 4 // 4 bytes, 32 bits, for now
    override fun isInt(): Boolean = true
}

class FloatType(val name:String) : TypeDef()
{
    override fun size(): Int = 4 // 4 bytes, 32 bits, for now
    override fun isFloat(): Boolean = true
}

// represent a member of struct, a member must have a name, and its type may reference to another type
// that is why it is TypeDesc type
class Member (val name: String, val typeDesc: TypeDesc)

class StructType(val name:String, var members:List<Member>) : TypeDef()
{
    override fun size(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}