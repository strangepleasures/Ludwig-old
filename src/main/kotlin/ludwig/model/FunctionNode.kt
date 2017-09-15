package ludwig.model


class FunctionNode : NamedNode() {
    var lazy: Boolean = false
    var visibility = Visibilities.PUBLIC

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitFunction(this)
    }
}
