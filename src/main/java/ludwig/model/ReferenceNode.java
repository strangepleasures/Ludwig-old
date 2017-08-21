package ludwig.model;


public class ReferenceNode extends Node<ReferenceNode> {
    private final Node<?> ref;

    public ReferenceNode(Node<?> ref) {
        this.ref = ref;
    }

    public Node<?> ref() {
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
