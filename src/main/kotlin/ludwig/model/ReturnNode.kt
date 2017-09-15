package ludwig.model

class ReturnNode : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitReturn(this)
    }

    override fun toString(): String {
        return "return"
    }
}
