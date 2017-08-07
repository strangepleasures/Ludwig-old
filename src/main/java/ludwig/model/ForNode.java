package ludwig.model;

public class ForNode extends Node<ForNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFor(this);
    }

    @Override
    public String toString() {
        return "for";
    }
}
