package ludwig.model

class OverrideNode : Node<OverrideNode>() {
    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitOverride(this)
    }

    override fun toString(): String {
        return "super"
    }
}
