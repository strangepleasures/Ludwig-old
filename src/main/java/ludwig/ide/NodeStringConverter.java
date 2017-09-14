package ludwig.ide;

import javafx.util.StringConverter;
import ludwig.model.NamedNode;
import ludwig.model.Node;
import ludwig.utils.NodeUtils;

class NodeStringConverter extends StringConverter<Node> {
    @Override
    public String toString(Node object) {
        return NodeUtils.INSTANCE.signature(object);
    }

    @Override
    public NamedNode fromString(String string) {
        return null;
    }
}
