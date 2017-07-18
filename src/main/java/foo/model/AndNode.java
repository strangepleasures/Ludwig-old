package foo.model;

public class AndNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAnd(this);
    }
}
