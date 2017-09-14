package ludwig.changes

class Rename : Change<Rename>() {
    private var nodeId: String? = null
    private var name: String? = null

    fun getNodeId(): String? {
        return nodeId
    }

    fun setNodeId(nodeId: String): Rename {
        this.nodeId = nodeId
        return this
    }

    fun name(): String? {
        return name
    }

    fun name(name: String): Rename {
        this.name = name
        return this
    }

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitRename(this)
    }
}
