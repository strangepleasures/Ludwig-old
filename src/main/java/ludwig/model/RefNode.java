package ludwig.model;


public class RefNode extends Node {
    private final NamedNode ref;

    public RefNode(NamedNode ref) {
        this.ref = ref;
    }

    public NamedNode ref() {
        return ref;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitRef(this);
    }
}
