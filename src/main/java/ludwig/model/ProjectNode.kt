package ludwig.model

class ProjectNode : NamedNode<ProjectNode>() {
    var readonly: Boolean = false

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitProject(this)
    }

    override val isOrdered: Boolean
        get() = false
}
