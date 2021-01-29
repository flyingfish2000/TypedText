package ast

import me.tomassetti.kolasu.model.*
import types.TypeDesc

interface Statement : Node

interface Expression : Node

// the root of AST
data class Compilation_unit(val entities: List<Entity>, override val position: Position? = null) : Node{

}

// a top_def can be either a variable declaration list, a structure defintion, or a function
data class Top_def(override val position: Position? = null) : Node {


}

// a block is a container, with variables and statements
// each variable has a name and typeDesc
data class Container(val variables:List<DefinedVariable>, val statements:List<Statement>, override val position: Position? = null) : Node{}

data class DummyStatment(val name: String, override val position: Position? = null) : Statement
data class BlockStatment(val block: Container, override val position: Position? = null) : Statement
data class ExprStatement(val expr: Expression, override val position: Position? = null) : Statement
data class IfStatement(val condExpr: Expression, val tStmt: Statement, val fStmt: Statement? = null, override val position: Position? = null):Statement
data class WhileStatement(val condExpr: Expression, val loopStmt: Statement, override val position: Position? = null):Statement
data class RtnStatement(val resultExpr: Expression, override val position: Position? = null):Statement

// expression
// left hand expression, variable, array and member expression are the only two LHS expr
abstract class LHSExp : Expression
data class UnknownExp(val exp:String = "unknown", override val position: Position? = null) : Expression

data class IntLiteral(val value: String, override val position: Position? = null) : Expression

data class CharLiteral(val value: String, override val position: Position? = null) : Expression

data class StringLiteral(val value: String, override val position: Position? = null) : Expression

data class FloatLiteral(val value: String, override val position: Position? = null) : Expression

data class VariableExp(val varName: String, override val position: Position? = null) : LHSExp() {
    var definition : Entity? = null // the reference to its definition, can be a variable or function,
    // but when the symbol is a function, it cannot be cannot be LHS, it may be ok, as the check is first done on FuncallExpr.
}

data class MemberExp(val varExp: Expression, val member: String, override val position: Position? = null) : LHSExp()

data class ArrayRefExp(val varExp: Expression, val idxExp: Expression, override val position: Position? = null) : LHSExp()

data class FuncallExpr(val funExp: Expression, val args: List<Expression>, override val position: Position? = null) : Expression

// assignment can be a statement, i.e. a = x+y;
// it can also be an expression, i.e. if( (a=x+y) == 10)
data class AssignExpr(val term: Expression, val valExp : Expression, override val position: Position? = null) : Expression

data class BinaryExp(val leftExp: Expression, val op: String, val rightExp : Expression? = null, override val position: Position? = null) : Expression

data class UnaryExp(val op: String, val exp : Expression, override val position: Position? = null) : Expression

// cast an expression to another type, (int)2.5, (int)(3.14*2.5* a), etc
data class TypeCastExp(val targetType: TypeDesc, val exp: Expression, override val position: Position? = null) : Expression

