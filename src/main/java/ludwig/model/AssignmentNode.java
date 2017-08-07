package ludwig.model;

public class AssignmentNode extends Node<AssignmentNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

    @Override
    public String toString() {
        return "=";
    }
}
