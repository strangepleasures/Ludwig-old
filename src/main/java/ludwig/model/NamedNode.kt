package ludwig.model

abstract class NamedNode<T : NamedNode<T>> : Node<T>() {
    var name: String = ""


    override fun toString(): String {
        return name
    }
}
