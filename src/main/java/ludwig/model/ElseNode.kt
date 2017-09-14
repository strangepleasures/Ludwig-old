package ludwig.model

class ElseNode : Node<ElseNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitElse(this)
    }

    override fun toString(): String {
        return "else"
    }
}
