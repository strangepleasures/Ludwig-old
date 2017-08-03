package ludwig.model;

public class VariableDeclarationNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitVariableDeclaration(this);
    }

    @Override
    public String toString() {
        return "=";
    }
}
