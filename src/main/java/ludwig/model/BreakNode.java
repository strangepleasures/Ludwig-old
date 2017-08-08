package ludwig.model;

public class BreakNode extends Node<BreakNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBreak(this);
    }
}
