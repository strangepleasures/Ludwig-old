package foo.changes;

import lombok.Value;

@Value
public class Parameter extends Change {
    String id;
    String name;
    Position position;

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitParameter(this);
    }
}
