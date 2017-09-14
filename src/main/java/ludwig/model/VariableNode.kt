package ludwig.model

class VariableNode : NamedNode<VariableNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitVariable(this)
    }
}
