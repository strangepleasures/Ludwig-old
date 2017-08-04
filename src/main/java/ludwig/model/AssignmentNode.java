package ludwig.model;

public class AssignmentNode extends Node implements Named {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

    @Override
    public String toString() {
        return "=";
    }

    @Override
    public String getName() {
        return children.get(0).toString();
    }
}
