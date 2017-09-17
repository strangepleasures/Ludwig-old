package ludwig.model


class ReferenceNode() : Node() {
    lateinit var ref: Node

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitReference(this)
    }

    override fun toString(): String {
        return ref.toString()
    }
}
