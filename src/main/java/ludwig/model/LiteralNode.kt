package ludwig.model

import com.fasterxml.jackson.annotation.JsonIgnore
import ludwig.utils.NodeUtils

class LiteralNode : Node<LiteralNode> {
    private var text: String
    @JsonIgnore
    private var value: Any? = null

    constructor(text: String) : this(text, NodeUtils.parseLiteral(text)) {}

    private constructor(text: String, value: Any?) {
        this.text = text
        this.value = value
    }

    fun text(): String {
        return text
    }

    fun value(): Any {
        if (value == null) {
            value = NodeUtils.parseLiteral(text)
        }
        return value!!
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T {
        return visitor.visitLiteral(this)
    }

    override fun toString(): String {
        return text
    }

    companion object {

        fun ofValue(value: Any): LiteralNode {
            return LiteralNode(NodeUtils.formatLiteral(value), value)
        }
    }
}
