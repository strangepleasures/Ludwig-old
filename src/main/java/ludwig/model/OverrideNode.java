package ludwig.model;

import java.util.List;

public class OverrideNode extends Node<OverrideNode> implements Signature {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOverride(this);
    }

    @Override
    public String name() {
        return ((Signature) children().get(0)).name();
    }

    @Override
    public List<String> arguments() {
        return ((Signature) children().get(0)).arguments();
    }
}
