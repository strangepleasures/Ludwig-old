package ludwig.model

import ludwig.utils.parseLiteral

abstract class Node : MutableList<Node> by mutableListOf<Node>() {
    lateinit var id: String
    var comment: String? = null
    var parent: Node? = null
    private var deleted = false

    fun <T : Node> parentOfType(type: Class<T>): T? {
        var n: Node? = this
        while (n != null && !type.isInstance(n)) {
            n = n.parent
        }
        return n as T?
    }

    fun deleted(): Boolean {
        return deleted
    }

    fun delete() {
        this.deleted = true
        if (parent != null && !parent!!.deleted) {
            parent!!.remove(this)
        }
        forEach({ it.delete() })
    }

    open val isOrdered: Boolean
        get() = true

    override fun toString(): String {
        return this::class.simpleName!!.substring(0, this::class.simpleName!!.length - 4).toLowerCase()
    }
}

class AssignmentNode : Node() {
    override fun toString(): String {
        return "="
    }
}

class BreakNode : Node()

class CallNode : Node()

class CatchNode : Node()

class ClassNode : NamedNode()

class ContinueNode : Node()

class ElseNode : Node()

class ForNode : Node()

class FunctionNode : NamedNode() {
    var lazy: Boolean = false
    var visibility = Visibilities.PUBLIC
}

class IfNode : Node()

class LambdaNode : Node() {
    override fun toString(): String {
        return "λ"
    }
}

class ListNode : Node()

class LiteralNode() : Node() {
    lateinit var text: String

    val value: Any? by lazy {
        parseLiteral(text)
    }

    override fun toString(): String {
        return text
    }
}

abstract class NamedNode : Node() {
    var name: String = ""

    override fun toString(): String {
        return name
    }
}

class OverrideNode : Node() {
    override fun toString(): String {
        return "super"
    }
}

class PackageNode : NamedNode() {
    override val isOrdered: Boolean
        get() = false
}

class PlaceholderNode : Node() {
    var parameter: String? = null

    override fun toString(): String {
        return "<$parameter>"
    }
}

class ProjectNode : NamedNode() {
    var readonly: Boolean = false

    override val isOrdered: Boolean
        get() = false
}

class SymbolNode() : Node() {
    lateinit var ref: Node

    override fun toString(): String {
        return ref.toString()
    }
}

class RefNode : Node()

class ReturnNode : Node()

class ThrowNode : Node()

class TryNode : Node()

class VariableNode : NamedNode()
