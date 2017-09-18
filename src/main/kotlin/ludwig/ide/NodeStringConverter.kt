package ludwig.ide

import javafx.util.StringConverter
import ludwig.model.NamedNode
import ludwig.model.Node
import ludwig.utils.signature

internal class NodeStringConverter : StringConverter<Node>() {
    override fun toString(obj: Node): String {
        return signature(obj)
    }

    override fun fromString(string: String): NamedNode? {
        return null
    }
}
