package ludwig.model


class ReferenceNode(private val ref: Node<*>) : Node<ReferenceNode>() {

    fun ref(): Node<*> {
        return ref
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitReference(this)
    }

    override fun toString(): String {
        return ref.toString()
    }
}
