package ludwig.model

class CallNode : Node<CallNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitCall(this)
    }

    override fun toString(): String {
        return "call"
    }
}
