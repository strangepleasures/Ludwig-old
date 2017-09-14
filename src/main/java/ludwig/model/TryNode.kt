package ludwig.model

class TryNode : Node<TryNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitTry(this)
    }

    override fun toString(): String {
        return "try"
    }
}
