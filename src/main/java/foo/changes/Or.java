package foo.changes;

import lombok.Value;

@Value
public class Or extends Change {
    String id;
    Destination destination;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitOr(this);
    }
}
