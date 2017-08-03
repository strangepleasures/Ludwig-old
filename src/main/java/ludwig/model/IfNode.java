package ludwig.model;

public class IfNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitIf(this);
    }

    @Override
    public String toString() {
        return "if";
    }
}
