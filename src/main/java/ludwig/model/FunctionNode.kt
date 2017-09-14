package ludwig.model


class FunctionNode : NamedNode<FunctionNode>() {
    private var lazy: Boolean = false
    private var visibility = Visibilities.PUBLIC

    fun lazy(): Boolean {
        return lazy
    }

    fun lazy(lazy: Boolean): FunctionNode {
        this.lazy = lazy
        return this
    }

    fun visibility(): Visibilities {
        return visibility
    }

    fun visibility(visibility: Visibilities): FunctionNode {
        this.visibility = visibility
        return this
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitFunction(this)
    }

    fun signature(): String {
        val builder = StringBuilder(name()!!)
        for (node in children()) {
            if (node !is VariableNode) {
                break
            }
            builder.append(' ').append(node)
        }
        return builder.toString()
    }
}
