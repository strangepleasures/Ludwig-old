package ludwig.model

import ludwig.utils.NodeUtils

class LiteralNode() : Node() {
    lateinit var text: String

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
}
