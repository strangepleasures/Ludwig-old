package ludwig.model;

public class CatchNode extends Node<CatchNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitCatch(this);
    }

    @Override
    public String toString() {
        return "catch";
    }
}
