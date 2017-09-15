package ludwig.model

class LambdaNode : Node() {

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitLambda(this)
    }

    override fun toString(): String {
        return "λ"
    }
}
