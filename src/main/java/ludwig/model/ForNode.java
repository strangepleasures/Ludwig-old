package ludwig.model;

public class ForNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFor(this);
    }

    @Override
    public String toString() {
        return "for";
    }
}
