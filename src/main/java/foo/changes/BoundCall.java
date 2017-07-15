package foo.changes;

import lombok.Value;

@Value
public class BoundCall extends Change {
    String id;
    String function;
    Destination destination;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitBoundCall(this);
    }
}
