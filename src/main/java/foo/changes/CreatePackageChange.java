package foo.changes;

public class CreatePackageChange extends Change {
    private String id;
    private String name;
    private String parentId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitCreatePackage(this);
    }
}
