package ludwig.model;

import java.util.List;

public class OverrideNode extends Node<OverrideNode> implements Signature {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOverride(this);
    }

    @Override
    public String getName() {
        return ((Signature) children().get(0)).getName();
    }

    @Override
    public List<String> arguments() {
        return ((Signature) children().get(0)).arguments();
    }
}
