package ast

import me.tomassetti.kolasu.model.*

interface Statement : Node

interface Expression : Node

// the root of AST
data class Compilation_unit(val entities: List<Entity>, override val position: Position? = null) : Node{


}

// a top_def can be either a variable declaration list, a structure defintion, or a function
data class Top_def(override val position: Position? = null) : Node {


}

// what else tree Nodes do we need?