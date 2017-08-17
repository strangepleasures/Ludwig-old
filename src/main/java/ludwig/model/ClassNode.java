package ludwig.model;

import java.util.Collections;
import java.util.List;

public class ClassNode extends NamedNode<ClassNode> implements Signature {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitClass(this);
    }

    @Override
    public List<String> arguments() {
        return Collections.emptyList();
    }
}
