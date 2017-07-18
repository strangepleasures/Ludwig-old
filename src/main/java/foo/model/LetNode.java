package foo.model;

public class LetNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLet(this);
    }
}
