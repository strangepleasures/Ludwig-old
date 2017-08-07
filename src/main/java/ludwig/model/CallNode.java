package ludwig.model;

public class CallNode extends Node<CallNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitCall(this);
    }

    @Override
    public String toString() {
        return "call";
    }
}
