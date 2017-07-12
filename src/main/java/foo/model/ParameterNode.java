package foo.model;

public class ParameterNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitParameter(this);
    }
}
