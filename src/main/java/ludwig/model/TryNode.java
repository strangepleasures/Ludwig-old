package ludwig.model;

public class TryNode extends Node<TryNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitTry(this);
    }
}
