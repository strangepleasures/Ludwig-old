package ludwig.ide;

import javafx.scene.control.ListCell;
import ludwig.model.Node;
import ludwig.model.Signature;
import ludwig.utils.NodeUtils;

class SignatureListCell extends ListCell<Signature> {
    @Override
    protected void updateItem(Signature item, boolean empty) {
        super.updateItem(item, empty);

        setText((!empty && item != null) ? NodeUtils.signature((Node) item) : "");
    }
}
