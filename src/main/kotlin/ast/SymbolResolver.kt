package ast

/*
the purpose symbol resolver is to check whether a reference to a variable, including function, is defined.
variables live in scopes, every function has its own scope.

Note:
1. symbol resolution doesn't resolve the members of a structure type, i.e. if pt1.x or pt2.y.z is found in an expression
it only checks with pt1 or pt2 are defined/declared. in order to know whether x is a valid member of the pt1, we need
first resolve the types, and then do the de-reference. for the same reason, pt1.func(...) will pass the symbol resolution
if pt1 is defined - the content of the structure is verified in later stages.
2. it doesn't check whether a function call has the right arguments.
3. it doesn't check the array ref is applied to a right expression, i.e. a[4] and 4[3] are both ok to the symbol resolver.
*/
import me.tomassetti.kolasu.validation.Error
import java.util.*

class SymbolTable(val parent: SymbolTable? = null) {
    private val values = HashMap<String, Entity>()

    fun hasName(name: String) = get(name) != null

    fun readByName(name: String) : Entity {
        val res = get(name)
        if (res == null) {
            throw RuntimeException("Unknown symbol $name. Known symbols: ${values.keys}")
        } else {
            return res
        }
    }

    val isTop: Boolean
        get() = parent == null

    val root : SymbolTable
        get() = if (parent == null) this else parent.root

    fun get(name: String) : Entity? {
        if (!values.containsKey(name)) {
            return parent?.get(name)
        }
        return values[name]!!
    }

    fun addSymbol(name: String, value: Entity) {
        values[name] = value
    }

}

fun Compilation_unit.resolveSymbols() : List<Error> {
    val errors = LinkedList<Error>()
    val globalScope = SymbolTable() // global scope has no parent
    val combined = this.entities.filter{ it is DefinedVariables}
    val allVariables = combined.map{(it as DefinedVariables).vars}.flatten()
    // add all variables to the top symbol table
    allVariables.forEach{
        globalScope.addSymbol(it.name, it)
    }
    // add all functions to the top symbol table
    val funs = this.entities.filter { it is DefinedFunction }
    funs.forEach{
        globalScope.addSymbol((it as DefinedFunction).name, it)
    }

    fun resolveExpression(expr: Expression, symbolTable: SymbolTable) {
        when (expr){
            is VariableExp ->{
                if(!symbolTable.hasName(expr.varName)) {
                    errors.add(Error("unable to resolve symbol: ${expr.varName}", expr.position))
                }
            }
            is MemberExp ->{
                resolveExpression(expr.varExp, symbolTable)
            }
            is ArrayRefExp ->{
                resolveExpression(expr.varExp, symbolTable)
                resolveExpression(expr.idxExp, symbolTable)
            }
            is FuncallExpr ->{
                resolveExpression(expr.funExp, symbolTable)
                expr.args.forEach{
                    resolveExpression(it, symbolTable)
                }
            }
            is AssignExpr ->{
                resolveExpression(expr.term, symbolTable)
                resolveExpression(expr.valExp, symbolTable)
            }
            is BinaryExp -> {
                resolveExpression(expr.leftExp, symbolTable)
                if(expr.rightExp!=null)
                    (resolveExpression(expr.rightExp, symbolTable))
            }
        }
    }

    fun resolveContainer(container: Container, symbolTable: SymbolTable){
        // container has its own local symbol table
        container.variables.forEach{
            symbolTable.addSymbol(it.name, it)
        }
        container.statements.forEach{
            when(it){
                is BlockStatment ->{
                    // create new scope
                    val localScope = SymbolTable(symbolTable)
                    resolveContainer(it.stmts, localScope)
                }
                is ExprStatement -> {
                    resolveExpression(it.expr, symbolTable)
                }
            }
        }
    }

    // resolve all functions
    funs.forEach{ entity ->
        val func = entity as DefinedFunction
        // create a new function scope, and add parameters to this scope
        val funScope = SymbolTable(globalScope)
        func.params.forEach{
            funScope.addSymbol(it.name, it)
        }
        resolveContainer(func.body, funScope)
    }


    return errors
}