package foo.model;

public class ProjectNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }
}
