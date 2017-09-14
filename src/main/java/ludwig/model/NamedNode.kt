package ludwig.model

abstract class NamedNode<T : NamedNode<T>> : Node<T>() {
    private var name: String = ""

    fun name(): String? {
        return name
    }

    fun name(name: String): T {
        this.name = name
        return this as T
    }

    override fun toString(): String {
        return name
    }
}
