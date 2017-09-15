package ludwig.model

abstract class NamedNode : Node() {
    var name: String = ""


    override fun toString(): String {
        return name
    }
}
