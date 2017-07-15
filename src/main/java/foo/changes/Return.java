package foo.changes;

import lombok.Value;

@Value
public class Return extends Change {
    String id;
    Position position;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitReturn(this);
    }
}
