package ludwig.changes

abstract class Insert<T : Insert<T>> : Change<T>() {
    internal var parent: String? = null
    internal var prev: String? = null
    internal var next: String? = null

    fun parent(): String? {
        return parent
    }

    fun parent(parent: String?): T {
        this.parent = parent
        return this as T
    }

    fun prev(): String? {
        return prev
    }

    fun prev(prev: String?): T {
        this.prev = prev
        return this as T
    }

    operator fun next(): String? {
        return next
    }

    fun next(next: String?): T {
        this.next = next
        return this as T
    }
}
