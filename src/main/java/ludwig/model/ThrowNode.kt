package ludwig.model

class ThrowNode : Node<ThrowNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitThrow(this)
    }
}
