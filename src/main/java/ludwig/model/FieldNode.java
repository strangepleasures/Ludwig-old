package ludwig.model;

import java.util.Collections;
import java.util.List;

public class FieldNode extends NamedNode<FieldNode> implements Signature {
    private static final List<String> ARGS = Collections.singletonList("it");

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitField(this);
    }

    @Override
    public List<String> arguments() {
        return ARGS;
    }
}
