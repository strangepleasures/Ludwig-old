package ludwig.model;

import java.util.List;

public class OverrideNode extends Node<OverrideNode>  {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOverride(this);
    }
}
