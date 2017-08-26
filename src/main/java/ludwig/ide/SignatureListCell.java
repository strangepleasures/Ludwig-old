package ludwig.ide;

import javafx.scene.control.ListCell;
import ludwig.model.Node;
import ludwig.utils.NodeUtils;

class SignatureListCell extends ListCell<Node<?>> {
    @Override
    protected void updateItem(Node<?> item, boolean empty) {
        super.updateItem(item, empty);

        setText((!empty && item != null) ? NodeUtils.signature(item) : "");
    }
}
