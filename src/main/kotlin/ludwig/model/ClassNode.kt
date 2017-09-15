package ludwig.model

class ClassNode : NamedNode() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitClass(this)
    }
}
