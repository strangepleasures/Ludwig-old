package ludwig.model;


public class ReferenceNode extends Node {
    private final NamedNode ref;

    public ReferenceNode(NamedNode ref) {
        this.ref = ref;
    }

    public NamedNode ref() {
        return ref;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

    @Override
    public String toString() {
        return ref.toString();
    }
}
