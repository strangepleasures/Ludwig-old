package ludwig.model

class BreakNode : Node<BreakNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitBreak(this)
    }

    override fun toString(): String {
        return "break"
    }
}
