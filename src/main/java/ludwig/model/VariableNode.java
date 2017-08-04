package ludwig.model;

public class VariableNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitVariable(this);
    }
}
