package ludwig.model;

public class ElseNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitElse(this);
    }
}
