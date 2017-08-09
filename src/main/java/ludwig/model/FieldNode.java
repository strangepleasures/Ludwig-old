package ludwig.model;

public class FieldNode extends NamedNode<FieldNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitField(this);
    }
}
