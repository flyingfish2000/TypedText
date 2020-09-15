package ast

import me.tomassetti.kolasu.model.*

interface Statement : Node

interface Expression : Node

data class Compilation_unit(override val position: Position? = null) : Node{


}

data class Top_def(override val position: Position? = null) : Node {

}