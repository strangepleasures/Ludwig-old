package ludwig.changes


import ludwig.model.Node

class InsertNode : Insert<InsertNode>() {
    private var node: Node<*>? = null

    fun node(): Node<*>? {
        return node
    }

    fun node(node: Node<*>): InsertNode {
        this.node = node
        return this
    }

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitInsertNode(this)
    }
}