package ludwig.model;

public class CallNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitCall(this);
    }

    @Override
    public String toString() {
        return "call";
    }
}
