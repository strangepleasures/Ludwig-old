package foo.model;

public class UnboundCallNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitUnboundCall(this);
    }
}
