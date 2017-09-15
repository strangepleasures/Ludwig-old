package ludwig.model

import ludwig.utils.NodeUtils

class LiteralNode(val text: String) : Node() {

    //@JsonIgnore
    val value: Any? by lazy {
        NodeUtils.parseLiteral(text)
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitLiteral(this)
    }

    override fun toString(): String {
        return text
    }

    companion object {

        fun ofValue(value: Any): LiteralNode {
            return LiteralNode(value.toString())
        }
    }
}
