package ludwig.model

class PlaceholderNode : Node<PlaceholderNode>() {
    fun parameter(): String? {
        return parameter
    }

    fun parameter(parameter: String): PlaceholderNode {
        this.parameter = parameter
        return this
    }

    private var parameter: String? = null


    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitPlaceholder(this)
    }

    override fun toString(): String {
        return "<$parameter>"
    }
}
