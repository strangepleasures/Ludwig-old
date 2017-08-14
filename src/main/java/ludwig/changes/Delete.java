package ludwig.changes;

public class Delete extends Change<Delete> {
    private String id;

    public String id() {
        return id;
    }

    public Delete id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitDelete(this);
    }
}
