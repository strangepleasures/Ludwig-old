package ludwig.model;

public class OverrideNode extends Node<OverrideNode>  {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOverride(this);
    }

    @Override
    public String toString() {
        return "super";
    }
}
