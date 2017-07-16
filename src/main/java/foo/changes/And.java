package foo.changes;

import lombok.Value;

@Value
public class And extends Change {
    String id;
    Destination destination;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitAnd(this);
    }
}
