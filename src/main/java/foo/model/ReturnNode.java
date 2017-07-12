package foo.model;

public class ReturnNode extends Node {
    private Node value;

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitReturn(this);
    }
}
