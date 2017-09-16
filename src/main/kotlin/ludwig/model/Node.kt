package ludwig.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
abstract class Node : MutableList<Node> by mutableListOf<Node>() {
    lateinit var id: String
    var comment: String? = null
    @JsonIgnore
    var parent: Node? = null
    @JsonIgnore
    private var deleted = false

    abstract fun <T> accept(visitor: NodeVisitor<T>): T


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
        @JsonIgnore
        get() = true
}
