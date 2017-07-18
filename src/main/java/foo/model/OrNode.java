package foo.model;

public class OrNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitOr(this);
    }
}
