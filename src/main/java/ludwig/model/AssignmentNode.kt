package ludwig.model

class AssignmentNode : Node<AssignmentNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitAssignment(this)
    }

    override fun toString(): String {
        return "="
    }
}
