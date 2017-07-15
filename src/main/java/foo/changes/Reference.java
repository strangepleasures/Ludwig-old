package foo.changes;

import lombok.Value;

@Value
public class Reference extends Change {
    String id;
    String nodeId;
    Destination destination;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitReference(this);
    }
}
