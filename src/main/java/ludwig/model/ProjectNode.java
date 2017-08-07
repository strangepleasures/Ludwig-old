package ludwig.model;

public class ProjectNode extends NamedNode<ProjectNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }
}
