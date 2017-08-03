package ludwig.model;


public class VariableNode extends Node {
    private final NamedNode ref;

    public VariableNode(NamedNode ref) {
        this.ref = ref;
    }

    public NamedNode ref() {
        return ref;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitVariable(this);
    }
}
