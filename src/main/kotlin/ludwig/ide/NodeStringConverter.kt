package ludwig.ide

import javafx.util.StringConverter
import ludwig.model.NamedNode
import ludwig.model.Node
import ludwig.utils.NodeUtils

internal class NodeStringConverter : StringConverter<Node>() {
    override fun toString(`object`: Node): String {
        return NodeUtils.signature(`object`)
    }

    override fun fromString(string: String): NamedNode? {
        return null
    }
}
