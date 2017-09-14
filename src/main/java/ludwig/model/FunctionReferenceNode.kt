package ludwig.model

class FunctionReferenceNode : Node<FunctionReferenceNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitFunctionReference(this)
    }

    override fun toString(): String {
        return "ref"
    }
}
