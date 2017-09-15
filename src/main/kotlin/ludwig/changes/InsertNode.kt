package ludwig.changes


import ludwig.model.Node

class InsertNode : Insert() {
    lateinit var node: Node

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitInsertNode(this)
    }
}