package ludwig.model;

public class VariableNode extends NamedNode<VariableNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitVariable(this);
    }
}
