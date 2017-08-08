package ludwig.model;

public class ContinueNode extends Node<ContinueNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitContinue(this);
    }
}
