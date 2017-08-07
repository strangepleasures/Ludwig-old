package ludwig.changes;

public class InsertReference extends Insert<InsertReference> {
    private String id;
    private String ref;

    public String getId() {
        return id;
    }

    public InsertReference setId(String id) {
        this.id = id;
        return this;
    }

    public String getRef() {
        return ref;
    }

    public InsertReference setRef(String ref) {
        this.ref = ref;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitInsertReference(this);
    }
}
