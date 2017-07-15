package foo.changes;

import lombok.Value;

@Value
public class CreateBoundCall extends Change {
    String id;
    String function;
    Destination destination;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitCreateBoundCall(this);
    }
}
