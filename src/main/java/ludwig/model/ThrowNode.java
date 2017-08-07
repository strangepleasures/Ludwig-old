package ludwig.model;

public class ThrowNode extends Node<ThrowNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitThrow(this);
    }
}
