package ludwig.ide

import javafx.scene.control.ListCell
import ludwig.model.Node
import ludwig.utils.signature

internal class SignatureListCell : ListCell<Node>() {
    override fun updateItem(item: Node?, empty: Boolean) {
        super.updateItem(item, empty)

        text = if (!empty && item != null) signature(item) else ""
    }
}
