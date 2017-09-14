package ludwig.model

class ProjectNode : NamedNode<ProjectNode>() {
    private var readonly: Boolean = false

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitProject(this)
    }

    fun readonly(): Boolean {
        return readonly
    }

    fun readonly(readonly: Boolean): ProjectNode {
        this.readonly = readonly
        return this
    }

    override val isOrdered: Boolean
        get() = false
}
