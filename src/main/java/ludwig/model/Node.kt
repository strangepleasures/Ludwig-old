package ludwig.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
abstract class Node<T : Node<T>> {
    private var id: String? = null
    private var comment: String? = null
    @JsonIgnore
    private var parent: Node<*>? = null
    private val children = ArrayList<Node<*>>()
    @JsonIgnore
    private var deleted = false

    abstract fun <T> accept(visitor: NodeVisitor<T>): T

    fun id(): String? {
        return id
    }

    fun id(id: String): T {
        this.id = id
        return this as T
    }

    fun comment(): String? {
        return comment
    }

    fun comment(comment: String): T {
        this.comment = comment
        return this as T
    }

    fun children(): MutableList<Node<*>> {
        return children
    }

    fun add(child: Node<*>): T {
        children.add(child)
        child.parent = this
        return this as T
    }

    fun parent(): Node<*>? {
        return parent
    }

    fun parent(parent: Node<*>?): T {
        this.parent = parent
        return this as T
    }

    fun <T : Node<*>> parentOfType(type: Class<T>): T? {
        var n: Node<*>? = this
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
            parent!!.children.remove(this)
        }
        children.forEach({ it.delete() })
    }

    open val isOrdered: Boolean
        @JsonIgnore
        get() = true
}
