package ludwig.model

class IfNode : Node<IfNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitIf(this)
    }

    override fun toString(): String {
        return "if"
    }
}
