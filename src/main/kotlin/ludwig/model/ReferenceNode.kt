package ludwig.model


class ReferenceNode(val ref: Node) : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitReference(this)
    }

    override fun toString(): String {
        return ref.toString()
    }
}
