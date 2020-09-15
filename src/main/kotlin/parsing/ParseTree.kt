package parsing

import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

abstract class ParseTreeElement {
    abstract fun multiLineString(indentation : String = ""): String
}

class ParseTreeLeaf(val type: String, val text: String) : ParseTreeElement() {
    override fun toString(): String{
        return "T:$type[$text]"
    }

    override fun multiLineString(indentation : String): String = "${indentation}T:$type[$text]\n"
}

class ParseTreeNode(val name: String) : ParseTreeElement() {
    val children = LinkedList<ParseTreeElement>()
    fun child(c : ParseTreeElement) : ParseTreeNode {
        children.add(c)
        return this
    }

    override fun toString(): String {
        return "Node($name) $children"
    }

    override fun multiLineString(indentation : String): String {
        val sb = StringBuilder()
        sb.append("${indentation}$name\n")
        children.forEach { c -> sb.append(c.multiLineString(indentation + "  ")) }
        return sb.toString()
    }
}

// node: the tree node from Antlr
// it iterates the Antlr tree node and ParseTreeLeaf and ParseTreeNode in Kotlin
fun toParseTree(node: ParserRuleContext, vocabulary: Vocabulary) : ParseTreeNode {
    val res = ParseTreeNode(node.javaClass.simpleName.removeSuffix("Context"))
    node.children?.forEach { c ->
        when (c) {
            is ParserRuleContext -> res.child(toParseTree(c, vocabulary)) // recursively go through a non-leaf node and convert to ParseTreeNode
            is TerminalNode -> {
                val symbolType = vocabulary.getSymbolicName(c.symbol.type) // symbol ';' has no symbol type
                if (symbolType != null)
                    res.child(ParseTreeLeaf(symbolType, c.text))
            }
        }
    }
    return res
}