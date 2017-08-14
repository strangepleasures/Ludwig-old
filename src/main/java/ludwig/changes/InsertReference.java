package ludwig.changes;

public class InsertReference extends Insert<InsertReference> {
    private String id;
    private String ref;

    public String id() {
        return id;
    }

    public InsertReference id(String id) {
        this.id = id;
        return this;
    }

    public String ref() {
        return ref;
    }

    public InsertReference ref(String ref) {
        this.ref = ref;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitInsertReference(this);
    }
}
