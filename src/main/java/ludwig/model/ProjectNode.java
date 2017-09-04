package ludwig.model;

public class ProjectNode extends NamedNode<ProjectNode> {
    private boolean readonly;

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitProject(this);
    }

    public boolean readonly() {
        return readonly;
    }

    public ProjectNode readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    @Override
    public boolean isOrdered() {
        return false;
    }
}
