package ludwig.model

class PackageNode : NamedNode() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitPackage(this)
    }

    override val isOrdered: Boolean
        get() = false
}
