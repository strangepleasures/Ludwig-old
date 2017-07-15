package foo.changes;

import lombok.Value;

@Value
public class CreateParameter extends Change {
    String id;
    String name;
    Position position;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitCreateParameter(this);
    }
}
