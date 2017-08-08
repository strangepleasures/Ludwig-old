package ludwig.changes;

public class Rename extends Change<Rename> {
    private String nodeId;
    private String name;

    public String getNodeId() {
        return nodeId;
    }

    public Rename setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Rename setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitRename(this);
    }
}
