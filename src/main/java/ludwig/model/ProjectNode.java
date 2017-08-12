package ludwig.model;

public class ProjectNode extends NamedNode<ProjectNode> {
    private boolean readonly;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public ProjectNode setReadonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    @Override
    public boolean isOrdered() {
        return false;
    }
}
