package ludwig.model;

public class UnboundCallNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitUnboundCall(this);
    }

    @Override
    public String toString() {
        return "call";
    }
}
