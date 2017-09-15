package ludwig.model

class CatchNode : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitCatch(this)
    }

    override fun toString(): String {
        return "catch"
    }
}
