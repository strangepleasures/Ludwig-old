package ludwig.model

class VariableNode : NamedNode() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitVariable(this)
    }
}
