package ludwig.model

class ContinueNode : Node() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitContinue(this)
    }

    override fun toString(): String {
        return "continue"
    }
}
