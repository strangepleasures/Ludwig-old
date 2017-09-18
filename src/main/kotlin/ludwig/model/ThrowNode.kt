package ludwig.model

class ThrowNode : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitThrow(this)
    }

    override fun toString(): String {
        return "throw"
    }
}
