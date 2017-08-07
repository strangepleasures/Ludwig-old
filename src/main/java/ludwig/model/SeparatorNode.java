package ludwig.model;

public class SeparatorNode extends Node<SeparatorNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitSeparator(this);
    }

    @Override
    public String toString() {
        return ":";
    }
}
