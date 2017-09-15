package ludwig.model

class CallNode : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitCall(this)
    }

    override fun toString(): String {
        return "call"
    }
}
