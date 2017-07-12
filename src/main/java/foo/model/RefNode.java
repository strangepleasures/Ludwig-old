package foo.model;

public class RefNode extends Node {
    private NamedNode node;

    public NamedNode getNode() {
        return node;
    }

    public void setNode(NamedNode node) {
        this.node = node;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitRef(this);
    }
}
