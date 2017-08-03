package ludwig.model;

public class ReturnNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitReturn(this);
    }

    @Override
    public String toString() {
        return "return";
    }
}
