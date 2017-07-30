package ludwig.model;

public class ForNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFor(this);
    }
}
