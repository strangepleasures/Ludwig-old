package ludwig.model

class ForNode : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitFor(this)
    }

    override fun toString(): String {
        return "for"
    }
}
