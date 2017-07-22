package foo.model;

public class AssignmentNode extends Node {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }
}
