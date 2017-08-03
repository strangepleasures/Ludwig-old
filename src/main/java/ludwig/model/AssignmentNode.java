package ludwig.model;

public class AssignmentNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

    @Override
    public String toString() {
        return "=";
    }
}
